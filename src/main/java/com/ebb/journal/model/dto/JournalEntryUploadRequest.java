package com.ebb.journal.model.dto;

import static com.ebb.journal.util.Constants.MONTH_DATE_REGEX;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class JournalEntryUploadRequest {

  @NotNull
  @NonNull
  @Pattern(regexp = MONTH_DATE_REGEX, message = "MonthDate must be in MM-DD format")
  private String monthDate;

  @NotNull
  @NonNull
  @Min(value = 1900, message = "Year must be no earlier than 1900")
  private Integer year;

  @NotNull
  @NonNull
  @Size(max = 5000, message = "Journal entries must be less than 5000 characters")
  String entry;
}
