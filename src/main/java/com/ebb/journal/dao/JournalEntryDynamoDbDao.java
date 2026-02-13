package com.ebb.journal.dao;

import static com.ebb.journal.util.Constants.JOURNAL_ENTRY_SK_PREFIX;
import static com.ebb.journal.util.Constants.KEY_SEPARATOR;
import static com.ebb.journal.util.StringUtil.buildJournalEntrySk;

import com.ebb.journal.configuration.AwsProperties;
import com.ebb.journal.model.JournalEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Repository
public class JournalEntryDynamoDbDao extends AbstractDao<JournalEntry> implements JournalEntryDao {

  public JournalEntryDynamoDbDao(
      DynamoDbEnhancedClient dynamoDbEnhancedClient,
      AwsProperties awsProperties) {
    super(dynamoDbEnhancedClient, awsProperties.getDynamodb().getTableName(), JournalEntry.class);
  }

  /**
   * Gets all journal entries for the user with the provided user ID.
   *
   * @param userId               the ID of the user.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return List of all journal entries.
   */
  @Override
  public List<JournalEntry> getAllJournalEntries(String userId, boolean isStronglyConsistent) {
    return super.getRecordsByPkAndSkPrefix(userId, JOURNAL_ENTRY_SK_PREFIX, isStronglyConsistent);
  }

  /**
   * Gets a specific journal entry by month, date and year.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the journal entries in format MM-DD.
   * @param year                 the year of the journal entry.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the journal entry as an optional.
   */
  @Override
  public Optional<JournalEntry> getJournalEntryByMonthDateYear(String userId, String monthDate,
      int year, boolean isStronglyConsistent) {
    String journalEntrySk = buildJournalEntrySk(monthDate, year);
    return super.getRecordByPkAndSk(userId, journalEntrySk, isStronglyConsistent);
  }

  /**
   * Gets the journal entries for the user with the provided user ID and monthDate.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the journal entries in format MM-DD.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return List of journal entries for the specified monthDate.
   */
  @Override
  public List<JournalEntry> getJournalEntriesByMonthDate(String userId, String monthDate,
      boolean isStronglyConsistent) {

    String prefixedSk = String.format("%s%s%s%s",
        JOURNAL_ENTRY_SK_PREFIX, KEY_SEPARATOR,
        monthDate, KEY_SEPARATOR);

    return super.getRecordsByPkAndSkPrefix(userId, prefixedSk, isStronglyConsistent);
  }

  /**
   * Adds or updates a journal entry.
   *
   * @param journalEntry the journal entry to add or update.
   */
  @Override
  public void upsertJournalEntry(JournalEntry journalEntry) {
    super.putRecord(journalEntry);
  }
}
