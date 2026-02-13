package com.ebb.journal.model;

import static com.ebb.journal.util.Constants.USER_SK;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@DynamoDbBean
public class User {

  @NotNull
  @NonNull
  @Getter(onMethod_ = {@DynamoDbPartitionKey})
  private String userId;

  @NotNull
  @NonNull
  @Getter(onMethod_ = {@DynamoDbSortKey, @JsonIgnore})
  private String sk = USER_SK;

  @NotNull
  @NonNull
  private String email;

  @NotNull
  @NonNull
  private String firstName;

  @NotNull
  @NonNull
  private String lastName;

  private Boolean isEmailVerified = false;

  @JsonInclude(Include.NON_NULL)
  private String phoneNumber;

  private Boolean isPhoneVerified = false;

  /**
   * Subscription status of the user
   **/
  private Boolean isSubscribed = false;

  @JsonInclude(Include.NON_NULL)
  private Address billingAddress;

  @JsonInclude(Include.NON_NULL)
  private Address deliveryAddress;

  @JsonInclude(Include.NON_EMPTY)
  private Instant lastLogin;

  private Instant created = Instant.now();

  private Instant lastModified = Instant.now();
}
