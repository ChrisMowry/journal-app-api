package com.ebb.journal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringUtilTests {
  private String journalEntrySk;
  private String photoEntrySk;

  @BeforeEach
  public void setup() {
    this.journalEntrySk = "JOURNAL_ENTRY#01-23#1995";
    this.photoEntrySk = "PHOTO_ENTRY#12-25#2023#1";
  }

  @Test
  public void testParseMonthDateFromSk() {
    String journalMonthDate = StringUtil.parseMonthDateFromSk(journalEntrySk);
    String photoMonthDate = StringUtil.parseMonthDateFromSk(photoEntrySk);

    assertEquals("01-23", journalMonthDate);
    assertEquals("12-25", photoMonthDate);
  }

  @Test
  public void testParseMonthDateFromInvalidSk() {
    String invalidJournalMonthDate1 = StringUtil.parseMonthDateFromSk("JOURNAL_ENTRY#0123#1995");
    String invalidJournalMonthDate2 = StringUtil.parseMonthDateFromSk("JOURNAL_ENTRY#01-23");
    String invalidJournalMonthDate3 = StringUtil.parseMonthDateFromSk("01-23#1");

    assertNull(invalidJournalMonthDate1);
    assertNull(invalidJournalMonthDate2);
    assertNull(invalidJournalMonthDate3);
  }

  @Test
  public void testParseYearFromSk() {
    Integer journalYear = StringUtil.parseYearFromSk(journalEntrySk);
    Integer photoYear = StringUtil.parseYearFromSk(photoEntrySk);

    assertEquals(1995, journalYear);
    assertEquals(2023, photoYear);
  }

  @Test
  public void testParseYearFromInvalidSk() {
    Integer invalidJournalYear1 = StringUtil.parseYearFromSk("JOURNAL_ENTRY#012");
    Integer invalidJournalYear2 = StringUtil.parseYearFromSk("1995");

    assertNull(invalidJournalYear1);
    assertNull(invalidJournalYear2);
  }

  @Test
  public void testParsePhotoIdFromSk() {
    Integer journalPhotoId = StringUtil.parsePhotoIdFromSk(journalEntrySk);
    Integer photoEntryPhotoId = StringUtil.parsePhotoIdFromSk(photoEntrySk);

    assertNull(journalPhotoId);
    assertEquals(1, photoEntryPhotoId);
  }
}
