package com.ebb.journal.controller;

import com.ebb.journal.model.dto.LoginDto;
import com.ebb.journal.model.dto.RefreshTokenRequest;
import com.ebb.journal.model.dto.RegisterUserRequest;
import com.ebb.journal.model.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/public/v1/auth")
public interface PublicAuthApi {

  /**
   * Registers a new user with the given registration data and returns access, id and refresh tokens
   * for the newly registered user.
   *
   * @param registerUserRequest the registration data to use for registering the user
   * @return access, id and refresh tokens for the newly registered user
   */
  @PostMapping("/register")
  public ResponseEntity<TokenResponse> registerUser(RegisterUserRequest registerUserRequest);

  /**
   * Logs in the user with the given login credentials and returns new access, id and refresh tokens
   * for the user.
   *
   * @param loginCredentials the login credentials to use for logging in the user
   * @return new access, id and refresh tokens for the user with the given login credentials
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(LoginDto loginCredentials);

  /**
   * Refreshes the access, id and refresh tokens for the user with the given refresh token.
   *
   * @param refreshTokenRequest the refresh token to use for refreshing the access, id and refresh tokens
   * @return the new access, id and refresh tokens
   */
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refreshToken(RefreshTokenRequest refreshTokenRequest);
}
