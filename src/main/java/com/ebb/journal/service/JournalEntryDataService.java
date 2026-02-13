package com.ebb.journal.service;

import static com.ebb.journal.util.Constants.JOURNAL_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.StringUtil.normalize;

import com.ebb.journal.dao.JournalEntryDao;
import com.ebb.journal.dao.retry.DaoRetryProxy;
import com.ebb.journal.exception.RetryableException;
import com.ebb.journal.exception.ServerErrorException;
import com.ebb.journal.model.JournalEntry;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.dto.JournalEntryUploadRequest;
import com.ebb.journal.model.mapper.JournalEntryMapper;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JournalEntryDataService {

  private final JournalEntryDao journalEntryDao;
  private final PhotoEntryDataService photoEntryDataService;
  private final JournalEntryMapper journalEntryMapper;
  private final DaoRetryProxy daoRetryProxy;

  public JournalEntryDataService(
      JournalEntryDao journalEntryDao,
      PhotoEntryDataService photoEntryDataService,
      JournalEntryMapper journalEntryMapper,
      DaoRetryProxy daoRetryProxy
  ) {
    this.journalEntryDao = journalEntryDao;
    this.photoEntryDataService = photoEntryDataService;
    this.journalEntryMapper = journalEntryMapper;
    this.daoRetryProxy = daoRetryProxy;
  }

  /**
   * Gets a list of journal entries for a specified month and date.
   *
   * @param userId    the userId of the user
   * @param monthDate the monthDate of the journal entries in format MM-DD
   * @return a list of journal entries
   */
  @Cacheable(value = JOURNAL_ENTRY_CACHE_NAME, key = "#userId + '-' + #monthDate")
  public List<JournalEntry> getJournalEntriesByMonthDate(String userId, String monthDate) {
    List<JournalEntry> journalEntries = this.journalEntryDao.getJournalEntriesByMonthDate(userId,
            monthDate, false).stream()
        .sorted(Comparator.comparingInt(JournalEntry::getYear).reversed())
        .toList();

    log.info("Found {} journal entries for month & date {} for userId {}", journalEntries.size(),
        monthDate, userId);

    // Get the photo entries for the monthDate
    Map<String, Map<Integer, List<PhotoEntry>>> photoEntriesMap =
        photoEntryDataService.getPhotoEntriesByMonthDateMap(userId, monthDate);

    // Map the photo entries to the journal entries
    log.info("Adding photos to the journal entries for userId {}", userId);
    mapPhotosToJournalEntries(journalEntries, photoEntriesMap);

    // Return the journal entries for the monthDate
    return journalEntries;
  }

  /**
   * Adds or updates a journal entry to the database.
   *
   * @param userId                    the userId of the user
   * @param journalEntryUploadRequest the journal entry request
   * @return a list of journal entries for the upserted month & date
   */
  @CachePut(value = JOURNAL_ENTRY_CACHE_NAME, key = "#userId + '-' + #journalEntryUploadRequest.monthDate")
  public List<JournalEntry> upsertJournalEntry(String userId,
      JournalEntryUploadRequest journalEntryUploadRequest) {

    // Sanitize & validate the journal entry upload request
    JournalEntryUploadRequest cleanedJournalEntryUploadRequest = JournalEntryUploadRequest.builder()
        .monthDate(normalize(journalEntryUploadRequest.getMonthDate()))
        .entry(normalize(journalEntryUploadRequest.getEntry()))
        .build();

    // If the journal entry exists, map the property values, otherwise build the journal entry object
    JournalEntry journalEntry;
    log.info("Checking if journal entry for month & date {} & year {} for userId {} exists...",
        journalEntryUploadRequest.getMonthDate(), cleanedJournalEntryUploadRequest.getYear(),
        userId);
    Optional<JournalEntry> journalEntryOptional = journalEntryDao.getJournalEntryByMonthDateYear(
        userId,
        cleanedJournalEntryUploadRequest.getMonthDate(), cleanedJournalEntryUploadRequest.getYear(),
        true);
    if (journalEntryOptional.isPresent()) {
      log.info("Journal entry for month & date {} & year {} for userId {} found!",
          cleanedJournalEntryUploadRequest.getMonthDate(),
          cleanedJournalEntryUploadRequest.getYear(), userId);
      journalEntry = journalEntryOptional.get();
      journalEntryMapper.mapJournalEntryUploadRequestToJournalEntry(journalEntry,
          cleanedJournalEntryUploadRequest);
      journalEntry.setLastModified(Instant.now());
    } else {
      log.info(
          "Journal entry for month & date {} & year {} for userId {} not found and is being created",
          cleanedJournalEntryUploadRequest.getMonthDate(),
          cleanedJournalEntryUploadRequest.getYear(), userId);
      journalEntry = JournalEntry.builder()
          .userId(userId)
          .monthDate(cleanedJournalEntryUploadRequest.getMonthDate())
          .year(cleanedJournalEntryUploadRequest.getYear())
          .entry(cleanedJournalEntryUploadRequest.getEntry())
          .build();
    }

    // Write the journal entry to the database
    try {
      log.info("Writing journal entry for month & date {} & year {} for userId {} to database",
          cleanedJournalEntryUploadRequest.getMonthDate(),
          cleanedJournalEntryUploadRequest.getYear(), userId);
      daoRetryProxy.executeRetryableDaoOperation(
          () -> journalEntryDao.upsertJournalEntry(journalEntry));
    } catch (RetryableException ex) {
      String message = String.format(
          "Error writing journal entry for user with ID: %s MonthDate: %s Year: %d",
          userId, journalEntry.getMonthDate(), journalEntry.getYear());
      log.error(message);
      throw new ServerErrorException(message);
    }

    // Retrieve the journal entries for the monthDate
    List<JournalEntry> journalEntries = journalEntryDao.getJournalEntriesByMonthDate(userId,
            journalEntry.getMonthDate(), true).stream()
        .sorted(Comparator.comparingInt(JournalEntry::getYear).reversed())
        .toList();

    // If no journal entries are found, throw an exception
    if (journalEntries.isEmpty()) {
      log.error("Journal entries not found after upsert for userId {}", userId);
      throw new ServerErrorException(String.format(
          "Unknown error upserting journal entry for user with ID: %s MonthDate: %s Year: %d",
          userId, journalEntry.getMonthDate(), journalEntry.getYear()));
    }

    // Get the photo entries for the monthDate
    log.info("Mapping photo entries to journal entries for user with userId {}", userId);
    Map<String, Map<Integer, List<PhotoEntry>>> photoEntriesMap =
        photoEntryDataService.getPhotoEntriesByMonthDateMap(userId, journalEntry.getMonthDate());

    // Map the photo entries to the journal entries
    mapPhotosToJournalEntries(journalEntries, photoEntriesMap);

    // Return the journal entries for the monthDate
    return journalEntries;
  }

  /**
   * Maps photo entries to their corresponding journal entries.
   *
   * @param journalEntries  journal entries
   * @param photoEntriesMap map of photo entries
   */
  protected void mapPhotosToJournalEntries(List<JournalEntry> journalEntries,
      Map<String, Map<Integer, List<PhotoEntry>>> photoEntriesMap) {
    for (JournalEntry entry : journalEntries) {
      entry.setPhotos(
          photoEntriesMap
              .get(entry.getMonthDate())
              .get(entry.getYear())
      );
    }
  }
}
