package com.ebb.journal.util;

import static com.ebb.journal.util.Constants.JOURNAL_ENTRY_SK_PREFIX;
import static com.ebb.journal.util.Constants.KEY_SEPARATOR;

import io.micrometer.common.util.StringUtils;
import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  static final Pattern MONTH_DAY_PATTERN = Pattern.compile("#(\\d{2}-\\d{2})#");
  static final Pattern YEAR_PATTERN = Pattern.compile("#(\\d{4})#?");
  static final Pattern PHOTO_ID_PATTERN = Pattern.compile("#(1|2|3)$");

  /**
   * Parses the monthDate (MM-DD) from the sort key.
   *
   * @param sk the sort key
   * @return the monthDate (MM-DD) or null if sk is null or invalid
   */
  public static String parseMonthDateFromSk(String sk) {
    if (sk == null || !sk.contains(Constants.KEY_SEPARATOR)) {
      return null;
    }

    Matcher matcher = MONTH_DAY_PATTERN.matcher(sk);

    return matcher.find() ? matcher.group(1) : null;
  }

  /**
   * Parses the year from the sort key.
   *
   * @param sk the sort key
   * @return the year or null if sk is null or invalid
   */
  public static Integer parseYearFromSk(String sk) {
    if (sk == null || !sk.contains(Constants.KEY_SEPARATOR)) {
      return null;
    }

    Matcher matcher = YEAR_PATTERN.matcher(sk);

    return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
  }

  /**
   * Parses the photo ID from the sort key.
   *
   * @param sk the sort key
   * @return the photo ID or null if sk is null or invalid
   */
  public static Integer parsePhotoIdFromSk(String sk) {
    if (sk == null || !sk.contains(Constants.KEY_SEPARATOR)) {
      return null;
    }
    Matcher matcher = PHOTO_ID_PATTERN.matcher(sk);

    return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
  }

  /**
   * Builds the journal entry sort key from the monthDate and year.
   *
   * @param monthDate the monthDate (MM-DD)
   * @param year      the year
   * @return the journal entry sort key or null if monthDate or year is null
   */
  public static String buildJournalEntrySk(String monthDate, Integer year) {
    return (StringUtils.isNotBlank(monthDate) && Objects.nonNull(year))
        ? String.format("%s%s%s%s%d", JOURNAL_ENTRY_SK_PREFIX, KEY_SEPARATOR, monthDate,
        KEY_SEPARATOR, year)
        : null;
  }

  /**
   * Builds the photo entry sort key from the monthDate, year, and photoId.
   *
   * @param monthDate the monthDate (MM-DD)
   * @param year the year
   * @param photoId the photo ID
   * @return the photo entry sort key or null if monthDate, year, or photoId is null
   */
  public static String buildPhotoEntrySk(String monthDate, Integer year, Integer photoId) {
    return (StringUtils.isNotBlank(monthDate) && Objects.nonNull(year) && Objects.nonNull(photoId))
        ? String.format("%s%s%s%s%d%s%d", Constants.PHOTO_ENTRY_SK_PREFIX, KEY_SEPARATOR, monthDate,
        KEY_SEPARATOR, year, KEY_SEPARATOR, photoId)
        : null;
  }

  /**
   * Trims, normalizes unicode an collapses whitespace
   *
   * @param value the string value to be normalized.
   * @return the normalized value
   */
  public static String normalize(String value){
    // Trim + Unicode normalization + collapse whitespace
    String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFKC);
    return normalized.replaceAll("\\s{2,}", " ");
  }
}
