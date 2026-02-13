package com.ebb.journal.dao;

import com.ebb.journal.model.JournalEntry;
import java.util.List;
import java.util.Optional;

public interface JournalEntryDao {

  /**
   * Gets all journal entries for the user with the provided user ID.
   *
   * @param userId               the ID of the user.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return List of all journal entries.
   */
  List<JournalEntry> getAllJournalEntries(String userId, boolean isStronglyConsistent);

  /**
   * Gets a specific journal entry by month, date and year.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the journal entries in format MM-DD.
   * @param year                 the year of the journal entry.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the journal entry as an optional.
   */
  Optional<JournalEntry> getJournalEntryByMonthDateYear(String userId, String monthDate, int year,
      boolean isStronglyConsistent);

  /**
   * Gets the journal entries for the user with the provided user ID and monthDate.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the journal entries in format MM-DD.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return List of journal entries for the specified monthDate.
   */
  List<JournalEntry> getJournalEntriesByMonthDate(String userId, String monthDate,
      boolean isStronglyConsistent);

  /**
   * Adds or updates a journal entry.
   *
   * @param journalEntry the journal entry to add or update.
   */
  void upsertJournalEntry(JournalEntry journalEntry);
}
