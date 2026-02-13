package com.ebb.journal.service;

import static com.ebb.journal.util.Constants.DATA_CACHE_MANAGER;
import static com.ebb.journal.util.Constants.JOURNAL_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.Constants.PHOTO_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.Constants.USER_CACHE_NAME;

import com.ebb.journal.dao.PhotoEntryDao;
import com.ebb.journal.dao.UserDao;
import com.ebb.journal.dao.retry.DaoRetryProxy;
import com.ebb.journal.exception.NotFoundException;
import com.ebb.journal.exception.RetryableException;
import com.ebb.journal.exception.ServerErrorException;
import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.User;
import com.ebb.journal.model.dto.UserUploadRequest;
import com.ebb.journal.model.mapper.UserMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDataService {

  private final UserDao userDao;
  private final UserMapper userMapper;
  private final PhotoEntryDao photoEntryDao;
  private final S3Service s3Service;
  private final DaoRetryProxy daoRetryProxy;

  public UserDataService(
      UserDao userDao,
      UserMapper userMapper,
      PhotoEntryDao photoEntryDao,
      S3Service s3Service,
      DaoRetryProxy daoRetryProxy) {
    this.userDao = userDao;
    this.userMapper = userMapper;
    this.photoEntryDao = photoEntryDao;
    this.s3Service = s3Service;
    this.daoRetryProxy = daoRetryProxy;
  }

  /**
   * Get the user with the provided user ID.
   *
   * @param userId the user ID of the user
   * @return the user data as an optional
   */
  @Cacheable(cacheManager = DATA_CACHE_MANAGER, value = USER_CACHE_NAME, key = "#userId")
  public User getUser(String userId) {
    log.info("Getting user with userId {}", userId);
    return userDao.getUser(userId, false)
        .orElseThrow(() -> new NotFoundException(
            String.format("User with userId %s not found!", userId)
        ));
  }

  /**
   * Updates or creates a user dataset
   *
   * @param userUploadRequest the user data to be updated or created
   * @return the user dataset
   */
  @CachePut(cacheManager = DATA_CACHE_MANAGER, value = USER_CACHE_NAME, key = "#userUploadRequest.userId")
  public User upsertUser(UserUploadRequest userUploadRequest) {

    // Sanitize & validate the user upload request
    UserUploadRequest cleanedUserUploadRequest = UserDataSanitizerService.sanitizeUserDataUploadRequest(
        userUploadRequest);

    // If the user exists, map the property values, otherwise build the User object
    User user;
    log.info("Checking if user with userId {} exists..", cleanedUserUploadRequest.getUserId());
    Optional<User> userOptional = userDao.getUser(cleanedUserUploadRequest.getUserId(), true);
    if (userOptional.isPresent()) {
      log.info("User found. Updating user with userId {}", cleanedUserUploadRequest.getUserId());
      user = userOptional.get();
      userMapper.mapUserUploadRequestToUser(user, cleanedUserUploadRequest);
      user.setLastModified(Instant.now());
    } else {
      log.info("User not found. Creating new user with userId {}",
          cleanedUserUploadRequest.getUserId());
      user = User.builder()
          .userId(cleanedUserUploadRequest.getUserId())
          .firstName(cleanedUserUploadRequest.getFirstName())
          .lastName(cleanedUserUploadRequest.getLastName())
          .email(cleanedUserUploadRequest.getEmail())
          .phoneNumber(cleanedUserUploadRequest.getPhoneNumber())
          .billingAddress(cleanedUserUploadRequest.getBillingAddress())
          .deliveryAddress(cleanedUserUploadRequest.getDeliveryAddress())
          .build();
    }

    // Write user to the database
    try {
      log.info("Writing user with userId {} changes to the database",
          userUploadRequest.getUserId());
      daoRetryProxy.executeRetryableDaoOperation(() -> userDao.upsertUser(user));
    } catch (RetryableException ex) {
      String message = String.format("Error writing user data for user with ID: %s",
          user.getUserId());
      log.error(message);
      throw new ServerErrorException(message);
    }

    // Return the user
    return userDao.getUser(cleanedUserUploadRequest.getUserId(), true).orElseThrow(() ->
        new ServerErrorException(String.format("Unknown error upserting user with ID %s",
            cleanedUserUploadRequest.getUserId())));
  }

  /**
   * Deletes the user and all associated data.
   *
   * @param userId the user ID of the user
   */
  @CacheEvict(
      cacheManager = DATA_CACHE_MANAGER,
      value = {USER_CACHE_NAME, JOURNAL_ENTRY_CACHE_NAME, PHOTO_ENTRY_CACHE_NAME},
      allEntries = true
  )
  public void deleteUser(String userId) {
    log.info("Deleting user with userId {}", userId);

    // Creates a list of files in s3 to be deleted
    List<PhotoEntry> photoEntries = photoEntryDao.getAllPhotoEntries(userId, true);
    List<String> filesToDelete = new java.util.ArrayList<>(
        photoEntries.stream().map(PhotoEntry::getFileName).toList());
    filesToDelete.addAll(photoEntries.stream().map(PhotoEntry::getThumbnailUrl).toList());

    // Deletes the user files in s3
    try {
      log.info("Deleting {} photo files for userId {}", photoEntries.size(), userId);
      daoRetryProxy.executeRetryableDaoOperation(() -> s3Service.asyncDeleteObjects(filesToDelete));
    } catch (RetryableException ex) {
      String message = String.format("Error deleting user photos for user with ID: %s", userId);
      log.error(message);
      throw new ServerErrorException(message);
    }

    try {
      log.info("Deleting all data for userId {}", userId);
      daoRetryProxy.executeRetryableDaoOperation(() -> userDao.deleteAll(userId));
    } catch (RetryableException ex) {
      String message = String.format("Error deleting user data for user with ID: %s", userId);
      log.error(message);
      throw new ServerErrorException(message);
    }
  }
}
