package com.ebb.journal.model;

import static com.ebb.journal.util.Constants.ADDRESS_LINE_REGEX;
import static com.ebb.journal.util.Constants.CITY_REGEX;
import static com.ebb.journal.util.Constants.MONTH_DATE_REGEX;
import static com.ebb.journal.util.Constants.STATE_REGEX;
import static com.ebb.journal.util.Constants.ZIP_REGEX;

import com.ebb.journal.validator.ValidState;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@DynamoDbBean
public class Address {

  @NotNull
  @NonNull
  @Pattern(regexp = ADDRESS_LINE_REGEX, message = "Invalid addressLine1 name: '{validatedValue}'")
  private String addressLine1;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Pattern(regexp = ADDRESS_LINE_REGEX, message = "Invalid addressLine2 name: '{validatedValue}'")
  private String addressLine2;

  @NotNull
  @NonNull
  @Pattern(regexp = CITY_REGEX, message = "Invalid city name: '{validatedValue}'")
  private String city;

  @NotNull
  @NotNull
  @ValidState
  private String state;

  @NotNull
  @NotNull
  @Pattern(regexp = ZIP_REGEX, message = "Invalid zip code: '{validatedValue}'")
  private String zipCode;
}
