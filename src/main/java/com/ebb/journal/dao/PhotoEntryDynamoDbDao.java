package com.ebb.journal.dao;

import static com.ebb.journal.util.Constants.KEY_SEPARATOR;
import static com.ebb.journal.util.Constants.PHOTO_ENTRY_SK_PREFIX;
import static com.ebb.journal.util.StringUtil.buildPhotoEntrySk;

import com.ebb.journal.configuration.AwsProperties;
import com.ebb.journal.model.PhotoEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.Builder;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Repository
public class PhotoEntryDynamoDbDao extends AbstractDao<PhotoEntry> implements PhotoEntryDao {

  public PhotoEntryDynamoDbDao(
      DynamoDbEnhancedClient dynamoDbEnhancedClient,
      AwsProperties awsProperties, Builder builder) {
    super(dynamoDbEnhancedClient, awsProperties.getDynamodb().getTableName(), PhotoEntry.class);
  }

  /**
   * Gets the photo entry for the user with the provided user ID and photo ID.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the photo entry in format MM-DD.
   * @param year                 the year of the photo entry.
   * @param photoId              the ID of the photo entry.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the requested photo entry as an optional.
   */
  @Override
  public Optional<PhotoEntry> getPhotoEntryById(String userId, String monthDate, int year,
      int photoId,
      boolean isStronglyConsistent) {

    String sk = buildPhotoEntrySk(monthDate, year, photoId);
    return super.getRecordByPkAndSk(userId, sk, isStronglyConsistent);
  }

  /**
   * Gets all photo entries for the user with the provided user ID and monthDate.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the photo entry in format MM-DD.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of photo entries for the monthDate.
   */
  @Override
  public List<PhotoEntry> getPhotoEntriesByMonthDate(String userId, String monthDate,
      boolean isStronglyConsistent) {

    String skPrefix = String.format("%s%s%s%s", PHOTO_ENTRY_SK_PREFIX, KEY_SEPARATOR, monthDate,
        KEY_SEPARATOR);
    return super.getRecordsByPkAndSkPrefix(userId, skPrefix, isStronglyConsistent);
  }

  /**
   * Gets all photo entries for the user for the provided month, date and year.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the photo entry in format MM-DD.
   * @param year                 the year of the photo entries.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of photo entries for the monthDate and year.
   */
  @Override
  public List<PhotoEntry> getPhotoEntriesByMonthDateYear(String userId, String monthDate, int year,
      boolean isStronglyConsistent) {
    String skPrefix = String.format("%s%s%s%s%d%s", PHOTO_ENTRY_SK_PREFIX, KEY_SEPARATOR, monthDate,
        KEY_SEPARATOR, year, KEY_SEPARATOR);
    return super.getRecordsByPkAndSkPrefix(userId, skPrefix, isStronglyConsistent);
  }

  /**
   * Gets all photo entries for the user with the provided user ID.
   *
   * @param userId               the ID of the user.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of all photo entries for the user.
   */
  @Override
  public List<PhotoEntry> getAllPhotoEntries(String userId, boolean isStronglyConsistent) {
    return super.getRecordsByPkAndSkPrefix(userId, PHOTO_ENTRY_SK_PREFIX, isStronglyConsistent);
  }

  /**
   * Adds or updates a photo entry.
   *
   * @param photoEntry the photo entry to add.
   */
  @Override
  public void upsertPhotoEntry(PhotoEntry photoEntry) {
    putRecord(photoEntry);
  }

  /**
   * Deletes the photo entry for the user with the provided user ID and photo ID.
   *
   * @param userId    the ID of the user.
   * @param monthDate the monthDate of the photo entry in format MM-DD.
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   */
  @Override
  public void deletePhotoEntry(String userId, String monthDate, int year, int photoId) {
    String sk = buildPhotoEntrySk(monthDate, year, photoId);
    super.deleteRecordByPkAndSk(userId, sk);
  }
}
