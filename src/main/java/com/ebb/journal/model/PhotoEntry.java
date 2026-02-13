package com.ebb.journal.model;

import static com.ebb.journal.util.Constants.MONTH_DATE_REGEX;
import static com.ebb.journal.util.StringUtil.buildPhotoEntrySk;
import static com.ebb.journal.util.StringUtil.parseMonthDateFromSk;
import static com.ebb.journal.util.StringUtil.parsePhotoIdFromSk;
import static com.ebb.journal.util.StringUtil.parseYearFromSk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
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
public class PhotoEntry {

  /**
   * The PhotoEntryBuilder class and custom methods are defined here. The Lombok @Builder decorator
   * will inject the methods for the rest of the fields in this builder.
   * <br>
   * Source: <a
   * href="https://projectlombok.org/features/Builder#:~:text=Each%20listed%20generated,the%20builder%20class.">Lombok
   * Builder Documentation</a>
   */
  public static class PhotoEntryBuilder {

    private void sk(@NotNull @NonNull String sk) {
      this.sk = sk;
      this.monthDate = parseMonthDateFromSk(sk);
      this.year = parseYearFromSk(sk);
      this.photoId = parsePhotoIdFromSk(sk);
    }

    public PhotoEntryBuilder monthDate(@NotNull @NonNull String monthDate) {
      if (Objects.nonNull(this.year) && Objects.nonNull(this.photoId)) {
        sk(Objects.requireNonNull(buildPhotoEntrySk(monthDate, this.year, this.photoId)));
      }
      this.monthDate = monthDate;
      return this;
    }

    public PhotoEntryBuilder year(@NotNull @NonNull Integer year) {
      if (StringUtils.isNotBlank(this.monthDate) && Objects.nonNull(this.photoId)) {
        sk(Objects.requireNonNull(buildPhotoEntrySk(monthDate, this.year, this.photoId)));
      }
      this.year = year;
      return this;
    }

    public PhotoEntryBuilder photoId(@NotNull @NonNull Integer photoId) {
      if (StringUtils.isNotBlank(this.monthDate) && Objects.nonNull(this.year)) {
        sk(Objects.requireNonNull(buildPhotoEntrySk(monthDate, this.year, this.photoId)));
      }
      this.photoId = photoId;
      return this;
    }
  }

  @NotNull
  @NonNull
  @Getter(onMethod_ = {@DynamoDbPartitionKey, @JsonIgnore})
  private String userId;

  @Getter(onMethod_ = {@DynamoDbSortKey, @JsonIgnore})
  private String sk;

  public void setSk(@NotNull @NonNull String sk) {
    this.sk = sk;
    this.monthDate = parseMonthDateFromSk(sk);
    this.year = parseYearFromSk(sk);
    this.photoId = parsePhotoIdFromSk(sk);
  }

  @Getter(onMethod_ = {@DynamoDbPartitionKey, @JsonIgnore})
  @Pattern(regexp = MONTH_DATE_REGEX, message = "monthDate must be in MM-DD format")
  private String monthDate;

  public void setMonthDate(@NotNull @NonNull String monthDate) {
    if (Objects.nonNull(this.year) && Objects.nonNull(this.photoId)) {
      this.sk = buildPhotoEntrySk(monthDate, this.year, this.photoId);
    }
    this.monthDate = monthDate;
  }

  @Getter(onMethod_ = {@DynamoDbPartitionKey, @JsonIgnore})
  private Integer year;

  public void setYear(@NotNull @NonNull Integer year) {
    if (StringUtils.isNotBlank(this.monthDate) && Objects.nonNull(this.photoId)) {
      this.sk = buildPhotoEntrySk(monthDate, this.year, this.photoId);
    }
    this.year = year;
  }

  @Min(value = 1, message = "photoId must be a positive integer")
  @Max(value = 3, message = "photoId must be no greater than 3")
  private Integer photoId;

  public void setPhotoId(@NotNull @NonNull Integer photoId) {
    if (StringUtils.isNotBlank(this.monthDate) && Objects.nonNull(this.year)) {
      this.sk = buildPhotoEntrySk(monthDate, this.year, this.photoId);
    }
    this.photoId = photoId;
  }

  private String description;

  /**
   * File name is added to the entry to allow the api access to the photo file name without
   * returning it to the user.
   */
  @Getter(onMethod_ = {@JsonIgnore})
  private String fileName;

  /**
   * Thumbnail file name is added to the entry to allow the api access to the photo thumbnail file
   * name without returning it to the user.
   */
  @Getter(onMethod_ = {@JsonIgnore})
  private String thumbnailFileName;

  /**
   * Added to the entry to allow the app access to the full-sized photo URL without storing it in
   * DynamoDB.
   */
  @Getter(onMethod_ = {@DynamoDbIgnore})
  @Setter(onMethod_ = {@DynamoDbIgnore})
  private String url;

  /**
   * Added to the entry to allow the app access to the photo thumbnail URL without storing it in
   * DynamoDB.
   */
  @Getter(onMethod_ = {@DynamoDbIgnore})
  @Setter(onMethod_ = {@DynamoDbIgnore})
  private String thumbnailUrl;

  @Builder.Default
  private Instant created = Instant.now();

  @Builder.Default
  private Instant lastModified = Instant.now();
}
