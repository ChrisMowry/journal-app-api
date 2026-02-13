package com.ebb.journal.dao;

import com.ebb.journal.model.User;
import java.util.Optional;

public interface UserDao {

  /**
   * Gets the user data with the provided user ID.
   *
   * @param userId               the ID of the user.
   * @param isStronglyConsistent whether to use strongly consistent read.
   * @return the requested user as an optional.
   */
  public Optional<User> getUser(String userId, boolean isStronglyConsistent);

  /**
   * Adds or updates user data.
   *
   * @param user the user to add or update.
   */
  public void upsertUser(User user);

  /**
   * Deletes all user data associated with the provided user ID.
   *
   * @param userId the ID of the user.
   */
  public void deleteAll(String userId);
}
