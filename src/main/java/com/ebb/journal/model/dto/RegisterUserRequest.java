package com.ebb.journal.model.dto;

import static com.ebb.journal.util.Constants.EMAIL_REGEX;
import static com.ebb.journal.util.Constants.NAME_REGEX;
import static com.ebb.journal.util.Constants.PHONE_REGEX;

import com.ebb.journal.model.Address;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
public class RegisterUserRequest {

  @NotNull
  @NonNull
  @Pattern(regexp = NAME_REGEX, message = "Invalid first name: '{validatedValue}'")
  private String firstName;

  @NotNull
  @NonNull
  @Pattern(regexp = NAME_REGEX, message = "Invalid last name: '{validatedValue}'")
  private String lastName;

  @NotNull
  @NonNull
  @Pattern(regexp = EMAIL_REGEX, message = "Invalid email: '{validatedValue}'")
  private String email;

  @Pattern(regexp = PHONE_REGEX, message = "Invalid phone number: '{validatedValue}'")
  private String phoneNumber;

  private String password;
  private Address billingAddress;
  private Address deliveryAddress;
}
