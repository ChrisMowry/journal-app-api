package com.ebb.journal.controller;

import com.ebb.journal.model.JournalEntry;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.User;
import com.ebb.journal.model.dto.JournalEntryUploadRequest;
import com.ebb.journal.model.dto.PhotoEntryUploadRequest;
import com.ebb.journal.model.dto.UserUploadRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/authenticated/v1")
public interface AuthenticatedJournalApi {

  /**
   * Gets the user data for the authenticated user.
   *
   * @param jwt ID token of the authenticated user
   * @return User data
   */
  @GetMapping("/user")
  public ResponseEntity<User> getUser(@AuthenticationPrincipal Jwt jwt);

  /**
   * Adds or updates the user data for the authenticated user.
   *
   * @param jwt         ID token of the authenticated user
   * @param requestUser User data to add or update
   * @return Updated or added user data
   */
  @PostMapping("/user")
  public ResponseEntity<User> upsertUser(@AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody UserUploadRequest requestUser);

  /**
   * Deletes the user data, journal entries & photo entries for the authenticated user.
   *
   * @param jwt ID token of the authenticated user
   * @return A ResponseEntity with no content
   */
  @DeleteMapping("/user")
  public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt);

  /**
   * Adds or updates a journal entry for the authenticated user.
   *
   * @param jwt                 ID token of the authenticated user
   * @param requestJournalEntry Journal entry to add or update
   * @return the journal entries for the added or updated journal entry month & date
   */
  @PostMapping("/journal")
  public ResponseEntity<List<JournalEntry>> upsertJournalEntry(@AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody JournalEntryUploadRequest requestJournalEntry);

  /**
   * Gets all journal entries for a specific month & date for the authenticated user.
   *
   * @param jwt       ID token of the authenticated user
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @return List of journal entries for the specified month & date
   */
  @GetMapping("/journal/{monthDate}")
  public ResponseEntity<List<JournalEntry>> getJournalEntriesByMonthDate(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String monthDate);

  /**
   * Adds or updates a photo entry for a specific month, date & year for the authenticated user.
   *
   * @param jwt                     ID token of the authenticated user
   * @param monthDate               Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year                    Year as a four-digit integer (e.g., 2024)
   * @param photoEntryUploadRequest Photo entry to add or update
   * @return Updated or added photo entry
   */
  @PostMapping(path = "/journal/{monthDate}/{year}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<PhotoEntry>> upsertPhotoEntry(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String monthDate,
      @PathVariable Integer year,
      @Valid @ModelAttribute PhotoEntryUploadRequest photoEntryUploadRequest);

  /**
   * Gets a specific photo entry by ID for a specific month, date & year for the authenticated
   * user.
   *
   * @param jwt       ID token of the authenticated user
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year      Year as a four-digit integer (e.g., 2024)
   * @param photoId   The ID of the photo entry to delete ( 1 - 3 )
   * @return The requested photo entry
   */
  @GetMapping("/journal/{monthDate}/{year}/photo/{photoId}")
  public ResponseEntity<PhotoEntry> getPhotoEntryById(@AuthenticationPrincipal Jwt jwt,
      @PathVariable String monthDate,
      @PathVariable Integer year,
      @PathVariable Integer photoId);

  /**
   * Deletes a specific photo entry by ID for a specific month, date & year for the authenticated
   * user.
   *
   * @param jwt       ID token of the authenticated user
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year      Year as a four-digit integer (e.g., 2024)
   * @param photoId   The ID of the photo entry to delete ( 1 - 3 )
   * @return A ResponseEntity with no content
   */
  @DeleteMapping("/journal/{monthDate}/{year}/photo/{photoId}")
  public ResponseEntity<Void> deletePhotoEntryById(@AuthenticationPrincipal Jwt jwt,
      @PathVariable String monthDate,
      @PathVariable Integer year,
      @PathVariable Integer photoId);
}