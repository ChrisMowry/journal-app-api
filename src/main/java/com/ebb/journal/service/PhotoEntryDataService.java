package com.ebb.journal.service;

import static com.ebb.journal.util.Constants.JPEG;
import static com.ebb.journal.util.Constants.JPEG_MIME_TYPE;
import static com.ebb.journal.util.Constants.PHOTO_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.StringUtil.normalize;

import com.ebb.journal.dao.PhotoEntryDao;
import com.ebb.journal.dao.retry.DaoRetryProxy;
import com.ebb.journal.exception.BadRequestException;
import com.ebb.journal.exception.NotFoundException;
import com.ebb.journal.exception.RetryableException;
import com.ebb.journal.exception.ServerErrorException;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.dto.PhotoEntryUploadRequest;
import com.ebb.journal.model.mapper.PhotoEntryMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PhotoEntryDataService {

  private final PhotoEntryDao photoEntryDao;
  private final PhotoEntryMapper photoEntryMapper;
  private final CloudFrontService cloudFrontService;
  private final S3Service s3Service;
  private final DaoRetryProxy daoRetryProxy;
  private final int PHOTO_URL_TTL = 900; // 15 minutes

  public PhotoEntryDataService(
      PhotoEntryDao photoEntryDao,
      PhotoEntryMapper photoEntryMapper,
      CloudFrontService cloudFrontService,
      S3Service s3Service,
      DaoRetryProxy daoRetryProxy
  ) {
    this.photoEntryDao = photoEntryDao;
    this.photoEntryMapper = photoEntryMapper;
    this.cloudFrontService = cloudFrontService;
    this.s3Service = s3Service;
    this.daoRetryProxy = daoRetryProxy;
  }

  /**
   * Gets a specific photo entry by its ID.
   *
   * @param userId    the ID of the user.
   * @param monthDate the monthDate of the photo entries in format MM-DD.
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   * @return the photo entry.
   * @throws NotFoundException if the photo entry is not found.
   */
  @Cacheable(value = PHOTO_ENTRY_CACHE_NAME, key = "#userId + '-' + #monthDate + '-' + #year + '-' + #photoId")
  public PhotoEntry getPhotoEntryById(String userId, String monthDate, int year, int photoId) {
    log.info("Getting photo entry for photoId {} for month & date {} & year {} for userId {}",
        photoId, monthDate, year, userId);
    Optional<PhotoEntry> photoEntryOptional = photoEntryDao.getPhotoEntryById(userId, monthDate,
        year, photoId, false);
    PhotoEntry photoEntry = photoEntryOptional.orElseThrow(() -> new NotFoundException(
        String.format(
            "Photo entry with ID %d for user with ID: %s not found for month date: %s, year: %d",
            photoId,
            userId,
            monthDate,
            year
        )));

    // Generate and set the signed URLs for each photo file.
    log.info(
        "Adding signed urls to photo entry for photoId {} for month & date {} & year {} for userId {}",
        photoId, monthDate, year, userId);
    setPhotoSignedUrls(photoEntry);

    return photoEntry;
  }

  /**
   * Gets all photo entries for a specific month & date.
   *
   * @param userId    ID of the user.
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @return Map of photo entries for the specified month & date.
   */
  public Map<String, Map<Integer, List<PhotoEntry>>> getPhotoEntriesByMonthDateMap(String userId,
      String monthDate) {

    log.info("Getting photo entry map for month & date {} for userId {}", monthDate, userId);
    List<PhotoEntry> photoEntries = photoEntryDao.getPhotoEntriesByMonthDate(userId,
        monthDate, false);

    // Generate and set signed URLs for each photo file.
    log.info("Generating signed urls for photo entry map for month & date {} for userId {}",
        monthDate, userId);
    for (PhotoEntry photoEntry : photoEntries) {
      setPhotoSignedUrls(photoEntry);
    }

    return photoEntries.stream()
        .collect(Collectors.groupingBy(
            PhotoEntry::getMonthDate,
            Collectors.groupingBy(PhotoEntry::getYear)
        ));
  }

  /**
   * Adds or updates the photo metadata to the database, creates a thumbnail of the photo and saves
   * the photo and thumbnail to s3.
   *
   * @param userId                  ID of the user.
   * @param monthDate               Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year                    the year of the photo entry.
   * @param photoEntryUploadRequest the photo entry request
   */
  @CachePut(value = PHOTO_ENTRY_CACHE_NAME, key = "#userId + '-' + #monthDate + '-' + #year + '-' + #photoEntryUploadRequest.photoId")
  public List<PhotoEntry> upsertPhotoEntry(String userId, String monthDate, int year,
      PhotoEntryUploadRequest photoEntryUploadRequest) {

    // Sanitize & validate the photo entry request
    PhotoEntryUploadRequest cleanedPhotoEntryUploadRequest = PhotoEntryUploadRequest.builder()
        .photo(photoEntryUploadRequest.getPhoto())
        .description(normalize(photoEntryUploadRequest.getDescription()))
        .photoId(photoEntryUploadRequest.getPhotoId())
        .build();

    // If the photo entry exists, map the property values, otherwise build the photo entry object
    PhotoEntry photoEntry;

    // Get the file names for the photo and thumbnail
    String fileName = getFileName(userId, monthDate, year,
        cleanedPhotoEntryUploadRequest.getPhotoId());
    String thumbnailName = getThumbnailFileName(userId, monthDate, year,
        photoEntryUploadRequest.getPhotoId());

    // Get the existing photo entry
    log.info(
        "Checking if photo entry with photoId {} for month & date {} & year {} for userId {} exists",
        cleanedPhotoEntryUploadRequest.getPhotoId(), monthDate, year, userId);
    Optional<PhotoEntry> photoEntryOptional = photoEntryDao.getPhotoEntryById(
        userId, monthDate, year, cleanedPhotoEntryUploadRequest.getPhotoId(), true);

    // Writes the photo metadata to the database
    if (photoEntryOptional.isPresent()) {
      log.info(
          "Photo entry found. Updating photo entry with photoId {} for month & date {} & year {} for userId {}.",
          cleanedPhotoEntryUploadRequest.getPhotoId(), monthDate, year, userId);
      photoEntry = photoEntryOptional.get();
      photoEntryMapper.mapPhotoEntryUploadRequestToPhotoEntry(photoEntry,
          cleanedPhotoEntryUploadRequest);
      photoEntry.setLastModified(Instant.now());
    } else {
      log.info(
          "Photo entry not found. Creating photo entry with photoId {} for month & date {} & year {} for userId {}.",
          cleanedPhotoEntryUploadRequest.getPhotoId(), monthDate, year, userId);
      photoEntry = PhotoEntry.builder()
          .userId(userId)
          .monthDate(monthDate)
          .year(year)
          .description(cleanedPhotoEntryUploadRequest.getDescription())
          .fileName(fileName)
          .thumbnailFileName(thumbnailName)
          .build();
    }

    // Write the photo entry to the database
    try {
      log.info(
          "Writing photo entry with photoId {} for month & date {} & year {} for userId {} to the database",
          cleanedPhotoEntryUploadRequest.getPhotoId(), monthDate, year, userId);
      daoRetryProxy.executeRetryableDaoOperation(
          () -> photoEntryDao.upsertPhotoEntry(photoEntry));
    } catch (RetryableException ex) {
      String message = String.format(
          "Error writing photo entry with ID %d for user with ID: %s MonthDate: %s Year: %d",
          cleanedPhotoEntryUploadRequest.getPhotoId(), userId, monthDate, year);
      log.error(message);
      throw new ServerErrorException(message);
    }

    try {
      // Generate thumbnail
      log.info("Generating thumbnail of photo {} to s3 for userId {}", fileName, userId);
      byte[] thumbnail = this.generateThumbnail(cleanedPhotoEntryUploadRequest.getPhoto());
      byte[] photoData = cleanedPhotoEntryUploadRequest.getPhoto().getBytes();

      // Write the photo to s3
      log.info("Writing photo {} to s3 for userId {}", fileName, userId);
      s3Service.putObject(fileName, photoData, JPEG_MIME_TYPE);

      // Write the thumbnail to s3
      log.info("Writing thumbnail {} to s3 for userId {}", thumbnailName, userId);
      s3Service.putObject(thumbnailName, thumbnail, JPEG_MIME_TYPE);

    } catch (IOException e) {
      log.error("Error accessing photo {} for userId {}",
          cleanedPhotoEntryUploadRequest.getPhotoId(),
          userId);
      throw new BadRequestException(String.format("Error accessing photo %d for userId %s",
          cleanedPhotoEntryUploadRequest.getPhotoId(), userId));
    }

    // Get the photos for the year
    List<PhotoEntry> photoEntries = photoEntryDao.getPhotoEntriesByMonthDateYear(userId, monthDate,
        year, true);

    // Generate and set signed URLs for each photo file.
    log.info(
        "Generating signed urls for photo entry with photoId {} for month & date {} & year {} for userId {}",
        cleanedPhotoEntryUploadRequest.getPhotoId(), monthDate, year, userId);
    for (PhotoEntry entry : photoEntries) {
      setPhotoSignedUrls(entry);
    }

    return photoEntries;
  }

  /**
   * Deletes a photo entry, photo and thumbnail by monthDate, year and photo ID.
   *
   * @param userId    ID of the user.
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   */
  @CacheEvict(value = PHOTO_ENTRY_CACHE_NAME, key = "#userId + '-' + #monthDate + '-' + #year + '-' + #photoId")
  public void deletePhotoEntryById(String userId, String monthDate, int year, int photoId) {
    // Delete the photo metadata from the database
    try {
      log.info("Deleting photo entry with id {} for month & date {} and year {} for userId {}",
          photoId, monthDate, year, userId);
      daoRetryProxy.executeRetryableDaoOperation(
          () -> photoEntryDao.deletePhotoEntry(userId, monthDate, year, photoId));
    } catch (RetryableException ex) {
      String message = String.format(
          "Error deleting photo entry with ID %d for user with ID: %s MonthDate: %s Year: %d",
          photoId, userId, monthDate, year);
      log.error(message);
      throw new ServerErrorException(message);
    }

    // Delete the photo file from s3
    try {
      log.info("Deleting photo file with id {} for month & date {} and year {} for userId {}",
          photoId, monthDate, year, userId);
      daoRetryProxy.executeRetryableDaoOperation(
          () -> s3Service.asyncDeleteObject(getFileName(userId, monthDate, year, photoId)));
    } catch (RetryableException ex) {
      String message = String.format(
          "Error deleting photo from s3 with ID %d for user with ID: %s MonthDate: %s Year: %d",
          photoId, userId, monthDate, year);
      log.error(message);
      throw new ServerErrorException(message);
    }

    // Delete the photo thumbnail file from s3
    try {
      log.info("Deleting photo thumbnail with id {} for month & date {} and year {} for userId {}",
          photoId, monthDate, year, userId);
      daoRetryProxy.executeRetryableDaoOperation(
          () -> s3Service.asyncDeleteObject(
              getThumbnailFileName(userId, monthDate, year, photoId)));
    } catch (RetryableException ex) {
      String message = String.format(
          "Error deleting thumbnail from s3 with ID %d for user with ID: %s MonthDate: %s Year: %d",
          photoId, userId, monthDate, year);
      log.error(message);
      throw new ServerErrorException(message);
    }
  }

  /**
   * Generates the file name property value for a photo entry.
   *
   * @param userId    ID of the user.
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   * @return file name string
   */
  protected String getFileName(String userId, String monthDate, int year, int photoId) {
    return String.format("/photos/%s/%s/%s/photo-%d", userId, monthDate, year, photoId);
  }

  /**
   * Generates the thumbnail file name property value for a photo entry.
   *
   * @param userId    ID of the user.
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   * @return thumbnail file name string
   */
  protected String getThumbnailFileName(String userId, String monthDate, int year,
      int photoId) {
    return String.format("/photos/%s/%s/%s/thumbnail-%d", userId, monthDate, year, photoId);
  }

  /**
   * Sets the photo entry signed url values.
   *
   * @param photoEntry the photo entry to have url values set
   */
  protected void setPhotoSignedUrls(PhotoEntry photoEntry) {
    photoEntry.setUrl(cloudFrontService.generateSignedUrl(photoEntry.getFileName(), PHOTO_URL_TTL));
    photoEntry.setThumbnailUrl(
        cloudFrontService.generateSignedUrl(photoEntry.getThumbnailFileName(), PHOTO_URL_TTL));
  }

  /**
   * Generates a thumbnail from the input file.
   *
   * @param photo the photo as a MultipartFile
   * @return the photo as a thumbnail in a byte array
   * @throws IOException throws an IOException on an access error
   */
  protected byte[] generateThumbnail(MultipartFile photo) throws IOException {
    ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
    Thumbnails.of(photo.getInputStream())
        .size(300, 300)
        .outputFormat(JPEG)
        .toOutputStream(thumbOut);

    return thumbOut.toByteArray();
  }
}
