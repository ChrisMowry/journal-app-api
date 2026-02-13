package com.ebb.journal.dao;

import com.ebb.journal.model.PhotoEntry;
import java.util.List;
import java.util.Optional;

public interface PhotoEntryDao {

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
  Optional<PhotoEntry> getPhotoEntryById(String userId, String monthDate, int year, int photoId,
      boolean isStronglyConsistent);

  /**
   * Gets all photo entries for the user with the provided user ID and monthDate.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the photo entry in format MM-DD.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of photo entries for the monthDate.
   */
  List<PhotoEntry> getPhotoEntriesByMonthDate(String userId, String monthDate,
      boolean isStronglyConsistent);

  /**
   * Gets all photo entries for the user for the provided month, date and year.
   *
   * @param userId               the ID of the user.
   * @param monthDate            the monthDate of the photo entry in format MM-DD.
   * @param year                 the year of the photo entries.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of photo entries for the monthDate and year.
   */
  List<PhotoEntry> getPhotoEntriesByMonthDateYear(String userId, String monthDate, int year,
      boolean isStronglyConsistent);

  /**
   * Gets all photo entries for the user with the provided user ID.
   *
   * @param userId               the email of the user.
   * @param isStronglyConsistent whether to use strongly consistent reads.
   * @return the list of all photo entries for the user.
   */
  List<PhotoEntry> getAllPhotoEntries(String userId, boolean isStronglyConsistent);

  /**
   * Adds or updates a photo entry.
   *
   * @param photoEntry the photo entry to add.
   */
  void upsertPhotoEntry(PhotoEntry photoEntry);

  /**
   * Deletes the photo entry for the user with the provided user ID and photo ID.
   *
   * @param userId    the ID of the user.
   * @param monthDate the monthDate of the photo entry in format MM-DD.
   * @param year      the year of the photo entry.
   * @param photoId   the ID of the photo entry.
   */
  void deletePhotoEntry(String userId, String monthDate, int year, int photoId);
}
