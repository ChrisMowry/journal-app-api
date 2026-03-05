package com.ebb.journal.controller;

import com.ebb.journal.model.dto.LoginDto;
import com.ebb.journal.model.dto.RefreshTokenRequest;
import com.ebb.journal.model.dto.RegisterUserRequest;
import com.ebb.journal.model.dto.TokenResponse;
import org.springframework.http.ResponseEntity;

public class PublicAuthController implements PublicAuthApi{

  /**
   * Registers a new user with the given registration data and returns access, id and refresh tokens
   * for the newly registered user.
   *
   * @param registerUserRequest the registration data to use for registering the user
   * @return access, id and refresh tokens for the newly registered user
   */
  @Override
  public ResponseEntity<TokenResponse> registerUser(RegisterUserRequest registerUserRequest) {
    // TODO: Implement user registration logic and return appropriate tokens for the newly registered user
    return null;
  }

  /**
   * Logs in the user with the given login credentials and returns new access, id and refresh tokens
   * for the user.
   *
   * @param loginCredentials the login credentials to use for logging in the user
   * @return new access, id and refresh tokens for the user with the given login credentials
   */
  @Override
  public ResponseEntity<TokenResponse> login(LoginDto loginCredentials) {
    // TODO: Implement user login logic and return appropriate tokens for the user with the given login credentials
    return null;
  }

  /**
   * Refreshes the access, id and refresh tokens for the user with the given refresh token.
   *
   * @param refreshTokenRequest the refresh token to use for refreshing the access, id and refresh tokens
   * @return the new access, id and refresh tokens
   */
  @Override
  public ResponseEntity<TokenResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
    // TODO: Implement token refresh logic and return appropriate tokens for the user with the given refresh token
    return null;
  }
}
