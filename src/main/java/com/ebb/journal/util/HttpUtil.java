package com.ebb.journal.util;

import com.ebb.journal.exception.BadRequestException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Utility class for HTTP-related operations.
 */
public class HttpUtil {

  /**
   * Extracts the email claim from a JWT.
   *
   * @param jwt ID token of the authenticated user
   * @return email claim from the JWT
   * @throws BadRequestException if the email claim is missing or blank
   */
  public static String getEmailFromJwt(Jwt jwt) {
    String email = jwt.getClaimAsString("email");
    if(email == null || email.isBlank()) {
      throw new BadRequestException("Email claim is missing in JWT");
    }

    return email;
  }
}
