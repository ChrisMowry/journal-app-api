package com.ebb.journal.dao;

import static com.ebb.journal.util.Constants.USER_SK;

import com.ebb.journal.configuration.AwsProperties;
import com.ebb.journal.exception.NotFoundException;
import com.ebb.journal.exception.RetryableException;
import com.ebb.journal.model.JournalEntry;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

@Slf4j
@Repository
public class UserDynamoDbDao extends AbstractDao<User> implements UserDao {

  private final JournalEntryDao journalEntryDao;
  private final PhotoEntryDao photoEntryDao;

  public UserDynamoDbDao(
      DynamoDbEnhancedClient dynamoDbEnhancedClient,
      AwsProperties awsProperties,
      JournalEntryDao journalEntryDao,
      PhotoEntryDao photoEntryDao) {
    super(dynamoDbEnhancedClient, awsProperties.getDynamodb().getTableName(), User.class);

    this.journalEntryDao = journalEntryDao;
    this.photoEntryDao = photoEntryDao;
  }

  /**
   * Gets the user data with the provided user ID.
   *
   * @param userId               the ID of the user.
   * @param isStronglyConsistent whether to use strongly consistent read.
   * @return the requested user as an optional.
   */
  @Override
  public Optional<User> getUser(String userId, boolean isStronglyConsistent) {
    return super.getRecordByPkAndSk(userId, USER_SK, isStronglyConsistent);
  }

  /**
   * Adds or updates user data.
   *
   * @param user the user to add or update.
   */
  @Override
  public void upsertUser(User user) {
    try {
      super.putRecord(user);
    } catch (Exception ex) {
      String message = String.format("Error upserting user with id %s. Retrying...",
          user.getUserId());
      log.warn(message);
      throw new RetryableException(message, ex);
    }
  }

  /**
   * Deletes all user data associated with the provided user ID.
   *
   * @param userId the ID of the user.
   */
  @Override
  public void deleteAll(String userId) {

    Optional<User> userOptional = this.getUser(userId, true);
    List<JournalEntry> journalEntries = this.journalEntryDao.getAllJournalEntries(userId, true);
    List<PhotoEntry> photoEntries = this.photoEntryDao.getAllPhotoEntries(userId, true);

    if (userOptional.isEmpty() && journalEntries.isEmpty() && photoEntries.isEmpty()) {
      throw new NotFoundException("No user data found with id: " + userId);
    }

    final int TRANSACT_LIMIT = 25;

    Map<Key, DynamoDbTable<?>> deleteKeys = new HashMap<>();

    // Adds the user record to the delete keys map
    deleteKeys.put(Key.builder().partitionValue(userId).sortValue(USER_SK).build(), this.table);

    // Adds all journal entries associated with the user to the delete keys map
    if (!journalEntries.isEmpty()) {
      journalEntries.forEach(journalEntry -> deleteKeys.put(
          Key.builder()
              .partitionValue(userId)
              .sortValue(journalEntry.getSk())
              .build(),
          this.dynamoDbEnhancedClient.table(this.table.tableName(),
              TableSchema.fromBean(JournalEntry.class))));
    }

    // Adds all photo entries associated with the user to the delete keys map
    if (!photoEntries.isEmpty()) {
      photoEntries.forEach(photoEntry -> deleteKeys.put(
          Key.builder()
              .partitionValue(userId)
              .sortValue(photoEntry.getSk())
              .build(),
          this.dynamoDbEnhancedClient.table(this.table.tableName(),
              TableSchema.fromBean(PhotoEntry.class))));
    }

    // Convert the map into a list of entries
    List<Entry<Key, DynamoDbTable<?>>> keyTableEntries = new ArrayList<>(deleteKeys.entrySet());

    // Partition the entries list into batches of TRANSACT_LIMIT
    List<List<Map.Entry<Key, DynamoDbTable<?>>>> partitionedEntries =
        Lists.partition(keyTableEntries, TRANSACT_LIMIT);

    log.info("Deleting {} journal entries, {} photo entries and account data for userId {}",
        journalEntries.size(), photoEntries.size(), userId);

    // Process each batch
    for (List<Map.Entry<Key, DynamoDbTable<?>>> entryBatch : partitionedEntries) {

      TransactWriteItemsEnhancedRequest.Builder transactBuilder =
          TransactWriteItemsEnhancedRequest.builder();

      // Add entries to delete transaction
      for (Map.Entry<Key, DynamoDbTable<?>> entry : entryBatch) {
        transactBuilder.addDeleteItem(entry.getValue(), entry.getKey());
      }
      // Execute the delete transaction
      try {
        this.dynamoDbEnhancedClient.transactWriteItems(transactBuilder.build());
      } catch (Exception ex) {
        log.warn("Error deleting user data for userId: {}", userId);
        throw new RetryableException("Error deleting user data for userId: " + userId,
            ex);
      }
    }
  }
}
