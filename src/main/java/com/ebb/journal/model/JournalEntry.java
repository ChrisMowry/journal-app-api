package com.ebb.journal.model;

import static com.ebb.journal.util.Constants.MONTH_DATE_REGEX;
import static com.ebb.journal.util.StringUtil.buildJournalEntrySk;
import static com.ebb.journal.util.StringUtil.parseMonthDateFromSk;
import static com.ebb.journal.util.StringUtil.parseYearFromSk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@DynamoDbBean
public class JournalEntry {

  /**
   * The JournalEntryBuilder class and custom methods are defined here. The Lombok @Builder
   * decorator will inject the methods for the rest of the fields in this builder.
   * <br>
   * Source: <a
   * href="https://projectlombok.org/features/Builder#:~:text=Each%20listed%20generated,the%20builder%20class.">Lombok
   * Builder Documentation</a>
   */
  public static class JournalEntryBuilder {

    private void sk(@NotNull @NonNull String sk) {
      this.sk = sk;
      this.monthDate = parseMonthDateFromSk(sk);
      this.year = parseYearFromSk(sk);
    }

    public JournalEntryBuilder monthDate(
        @Pattern(regexp = MONTH_DATE_REGEX, message = "monthDate must be in MM-DD format")
        @NotNull @NonNull String monthDate) {
      if (Objects.nonNull(this.year)) {
        sk(Objects.requireNonNull(buildJournalEntrySk(this.monthDate, this.year)));
      }
      this.monthDate = monthDate;
      return this;
    }

    public JournalEntryBuilder year(@NotNull @NonNull Integer year) {
      if (StringUtils.isNotBlank(this.monthDate)) {
        sk(Objects.requireNonNull(buildJournalEntrySk(this.monthDate, this.year)));
      }
      this.year = year;
      return this;
    }
  }

  @NotNull
  @NonNull
  @Getter(onMethod_ = {@DynamoDbPartitionKey, @JsonIgnore})
  private String userId;

  @NotNull
  @NonNull
  @Getter(onMethod_ = {@DynamoDbSortKey, @JsonIgnore})
  private String sk;

  public void setSk(@NonNull String sk) {
    this.sk = sk;
    this.monthDate = parseMonthDateFromSk(sk);
    this.year = parseYearFromSk(sk);
  }

  @Pattern(regexp = MONTH_DATE_REGEX, message = "monthDate must be in MM-DD format")
  private String monthDate;

  public void setMonthDate(@NonNull String monthDate) {
    if (Objects.nonNull(this.year)) {
      this.sk = Objects.requireNonNull(buildJournalEntrySk(this.monthDate, this.year));
    }
    this.monthDate = monthDate;
  }

  private Integer year;

  public void setYear(@NonNull Integer year) {
    if (StringUtils.isNotBlank(this.monthDate)) {
      this.sk = Objects.requireNonNull(buildJournalEntrySk(this.monthDate, this.year));
    }
    this.year = year;
  }

  private String entry;

  @Getter(onMethod_ = {@DynamoDbIgnore})
  @Builder.Default
  private List<PhotoEntry> photos = Collections.emptyList();

  @Builder.Default
  private Instant created = Instant.now();

  @Builder.Default
  private Instant lastModified = Instant.now();
}
