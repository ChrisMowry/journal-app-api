package com.ebb.journal.controller;

import com.ebb.journal.model.JournalEntry;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.User;
import com.ebb.journal.model.dto.JournalEntryUploadRequest;
import com.ebb.journal.model.dto.PhotoEntryUploadRequest;
import com.ebb.journal.model.dto.UserUploadRequest;
import com.ebb.journal.service.JournalEntryDataService;
import com.ebb.journal.service.PhotoEntryDataService;
import com.ebb.journal.service.UserDataService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthenticatedJournalController implements AuthenticatedJournalApi {

  private final UserDataService userDataService;
  private final JournalEntryDataService journalEntryDataService;
  private final PhotoEntryDataService photoEntryDataService;

  public AuthenticatedJournalController(
      UserDataService userDataService,
      JournalEntryDataService journalEntryDataService,
      PhotoEntryDataService photoEntryDataService
  ) {
    this.userDataService = userDataService;
    this.journalEntryDataService = journalEntryDataService;
    this.photoEntryDataService = photoEntryDataService;
  }

  /**
   * Gets the user data for the authenticated user.
   *
   * @param jwt ID token of the authenticated user
   * @return User data
   */
  @Override
  public ResponseEntity<User> getUser(Jwt jwt) {
    String userId = jwt.getSubject();
    User user = userDataService.getUser(userId);
    return ResponseEntity.ok(user);
  }

  /**
   * Adds or updates the user data for the authenticated user.
   *
   * @param jwt         ID token of the authenticated user
   * @param requestUser User data to add or update
   * @return Updated or added user data
   */
  @Override
  public ResponseEntity<User> upsertUser(Jwt jwt, @Valid UserUploadRequest requestUser) {
    String userId = jwt.getSubject();
    User user = userDataService.upsertUser(requestUser);
    return ResponseEntity.ok(user);
  }

  /**
   * Deletes the user data, journal entries & photo entries for the authenticated user.
   *
   * @param jwt ID token of the authenticated user
   * @return A ResponseEntity with no content
   */
  @Override
  public ResponseEntity<Void> deleteUser(Jwt jwt) {
    String userId = jwt.getSubject();
    userDataService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Adds or updates a journal entry for the authenticated user.
   *
   * @param jwt                 ID token of the authenticated user
   * @param requestJournalEntry Journal entry to add or update
   * @return the journal entries for the added or updated journal entry month & date
   */
  @Override
  public ResponseEntity<List<JournalEntry>> upsertJournalEntry(Jwt jwt,
      @Valid JournalEntryUploadRequest requestJournalEntry) {
    String userId = jwt.getSubject();
    List<JournalEntry> journalEntries = this.journalEntryDataService.upsertJournalEntry(userId,
        requestJournalEntry);
    return ResponseEntity.ok(journalEntries);
  }

  /**
   * Gets all journal entries for a specific month & date for the authenticated user.
   *
   * @param jwt       ID token of the authenticated user
   * @param monthDate Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @return List of journal entries for the specified month & date
   */
  @Override
  public ResponseEntity<List<JournalEntry>> getJournalEntriesByMonthDate(Jwt jwt,
      String monthDate) {
    String userId = jwt.getSubject();
    return ResponseEntity.ok(
        journalEntryDataService.getJournalEntriesByMonthDate(userId, monthDate)
    );
  }

  /**
   * Adds or updates a photo entry for a specific month, date & year for the authenticated user.
   *
   * @param jwt               ID token of the authenticated user
   * @param monthDate         Month and Day in MM-DD format (e.g., "03-15" for March 15th)
   * @param year              Year as a four-digit integer (e.g., 2024)
   * @param photoEntryUploadRequest Photo entry to add or update
   * @return Updated or added photo entry
   */
  @Override
  public ResponseEntity<List<PhotoEntry>> upsertPhotoEntry(Jwt jwt, String monthDate, Integer year,
      @Valid PhotoEntryUploadRequest photoEntryUploadRequest) {
    String userId = jwt.getSubject();
    List<PhotoEntry> photoEntries = photoEntryDataService.upsertPhotoEntry(userId, monthDate, year,
        photoEntryUploadRequest);
    return ResponseEntity.ok(photoEntries);
  }

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
  @Override
  public ResponseEntity<PhotoEntry> getPhotoEntryById(Jwt jwt, String monthDate, Integer year,
      Integer photoId) {
    String userId = jwt.getSubject();
    PhotoEntry photoEntry = photoEntryDataService.getPhotoEntryById(userId, monthDate, year,
        photoId);
    return ResponseEntity.ok(photoEntry);
  }

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
  @Override
  public ResponseEntity<Void> deletePhotoEntryById(Jwt jwt, String monthDate, Integer year,
      Integer photoId) {
    String userId = jwt.getSubject();
    photoEntryDataService.deletePhotoEntryById(userId, monthDate, year, photoId);
    return ResponseEntity.noContent().build();
  }
}
