package com.ebb.journal.model.mapper;

import com.ebb.journal.model.JournalEntry;
import com.ebb.journal.model.dto.JournalEntryUploadRequest;
import com.ebb.journal.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface JournalEntryMapper {

  /**
   * Maps a journal entry with the journal entry upload request.
   *
   * @param journalEntry the journal entry to be updated
   * @param journalEntryUploadRequest the updates to be mapped
   */
  @BeanMapping(
      ignoreByDefault = true,
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
  )
  // this mapping allows the entry property to be null if a blank string is provided
  // or if the source value is null
  @Mapping(target = Constants.ENTRY,
      qualifiedByName = "blankStringToNull",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
  )
  @Mapping(target = Constants.MONTH_DATE, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.YEAR)
  void mapJournalEntryUploadRequestToJournalEntry(@MappingTarget JournalEntry journalEntry,
      JournalEntryUploadRequest journalEntryUploadRequest);

  /**
   * Converts a blank string to a null value.
   *
   * @param value the value to be evaluated and converted
   * @return null if the string is blank
   */
  @Named("blankStringToNull")
  default String blankStringToNull(String value) {
    return value != null && value.isBlank() ? null : value;
  }

  /**
   * Used to check if a value is not only null, but a blank string as well
   *
   * @param value the value to be evaluated
   * @return true if the value is not blank
   */
  @Condition
  @Named("isNotBlank")
  default boolean isNotBlank(String value) {
    return StringUtils.isNotBlank(value);
  }
}
