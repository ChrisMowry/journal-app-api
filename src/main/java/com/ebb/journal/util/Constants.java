package com.ebb.journal.util;

import java.util.Set;

public class Constants {

  public static final String KEY_SEPARATOR = "#";
  public static final String USER_SK = "META";
  public static final String NAME_REGEX = "^[\\p{L} .'-]{1,50}$";
  public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  public static final String PHONE_REGEX = "^\\+[1-9]\\d{7,14}$";
  public static final String NON_NUMERIC_EXCEPT_PLUS_SIGN_PATTERN = "[^0-9+]";
  public static final String MONTH_DATE_REGEX = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
  public static final String ADDRESS_LINE_REGEX = "^[\\p{L}0-9 .,'#\\-\\/]{1,100}$";
  public static final String CITY_REGEX = "^[\\p{L} .'-]{1,50}$";
  public static final String STATE_REGEX = "^[A-Za-z]{2}$"; // US states
  public static final String ZIP_REGEX = "^\\d{5}(-\\d{4})?$"; // US ZIP or ZIP+4

  public static final String JOURNAL_ENTRY_SK_PREFIX = "JOURNAL_ENTRY";
  public static final String PHOTO_ENTRY_SK_PREFIX = "PHOTO_ENTRY";
  public static final String TIMESTAMP = "timestamp";
  public static final String JWK_SIGNING_KEYS_CACHE_NAME = "jwkSigningKeysCache";
  public static final String JWKS_CACHE_MANAGER = "jwksCacheManager";
  public static final String DATA_CACHE_MANAGER = "dataCache";
  public static final String USER_CACHE_NAME = "usersCache";
  public static final String JOURNAL_ENTRY_CACHE_NAME = "journalEntriesCache";
  public static final String PHOTO_ENTRY_CACHE_NAME = "photoEntriesCache";

  public static final String USER_ID = "userId";
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String EMAIL = "email";
  public static final String PHONE_NUMBER = "phoneNumber";
  public static final String BILLING_ADDRESS = "billingAddress";
  public static final String DELIVERY_ADDRESS = "deliveryAddress";

  public static final String MONTH_DATE = "monthDate";
  public static final String YEAR = "year";
  public static final String ENTRY = "entry";

  public static final String PHOTO_ID = "photoId";
  public static final String DESCRIPTION = "description";

  public static final String JPEG_MIME_TYPE = "image/jpeg";
  public static final String JPEG = "jpg";

  public static final Set<String> STATES = Set.of("AB", "AK", "AL", "AR", "AZ", "BC",
      "CA", "CO", "CT", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA",
      "MA", "MB", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NB", "NC", "ND", "NE", "NH",
      "NJ", "NL", "NM", "NS", "NV", "NY", "OH", "OK", "ON", "OR", "PA", "PE", "QC", "RI",
      "SC", "SD", "SK", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY");
}
