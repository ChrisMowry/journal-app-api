package com.ebb.journal.service;

import static com.ebb.journal.util.Constants.NON_NUMERIC_EXCEPT_PLUS_SIGN_PATTERN;
import static com.ebb.journal.util.StringUtil.normalize;

import com.ebb.journal.model.Address;
import com.ebb.journal.model.dto.UserUploadRequest;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.stereotype.Service;

@Service
public class UserDataSanitizerService {

  /**
   * Sanitizes the user data to be added or updated.
   *
   * @param userUploadRequest the user data upload request to be sanitized
   * @return the sanitized user upload request
   */
  public static UserUploadRequest sanitizeUserDataUploadRequest(UserUploadRequest userUploadRequest) {
    return UserUploadRequest.builder()
        .userId(userUploadRequest.getUserId())
        .email(normalize(userUploadRequest.getEmail()))
        .firstName(normalize(userUploadRequest.getFirstName()))
        .lastName(normalize(userUploadRequest.getLastName()))
        .phoneNumber(sanitizePhoneNumber(userUploadRequest.getPhoneNumber()))
        .billingAddress(sanitizeAddress(userUploadRequest.getBillingAddress()))
        .deliveryAddress(sanitizeAddress(userUploadRequest.getDeliveryAddress()))
        .build();
  }

  /**
   * Removes all non-numeric characters and a plus sign from a phone number string.
   *
   * @param phoneNumber phone number string to be sanitized
   * @return phone number without any special characters and a plus sign.
   */
  protected static String sanitizePhoneNumber(String phoneNumber) {
    Pattern nonNumericExceptPlusSignPattern = Pattern.compile(NON_NUMERIC_EXCEPT_PLUS_SIGN_PATTERN);
    return RegExUtils.replaceAll(phoneNumber.trim(), nonNumericExceptPlusSignPattern, "");
  }

  /**
   * Sanitizes an Address
   *
   * @param address the address to be sanitized
   * @return the sanitized address
   */
  protected static Address sanitizeAddress(Address address){
    return Address.builder()
        .addressLine1(normalize(address.getAddressLine1()))
        .addressLine2(normalize(address.getAddressLine2()))
        .city(normalize(address.getCity()))
        .state(normalize(address.getState()))
        .zipCode(normalize(address.getZipCode()))
        .build();
  }
}
