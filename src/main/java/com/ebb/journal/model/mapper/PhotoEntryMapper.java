package com.ebb.journal.model.mapper;

import com.ebb.journal.model.PhotoEntry;
import com.ebb.journal.model.dto.PhotoEntryUploadRequest;
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
public interface PhotoEntryMapper {

  /**
   * Maps a journal entry with the journal entry upload request.
   *
   * @param photoEntry              the photo entry to be updated
   * @param photoEntryUploadRequest the updates to be mapped
   */
  @BeanMapping(
      ignoreByDefault = true,
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
  )
  // this mapping allows the description property to be null if a blank string is provided
  // or if the source value is null
  @Mapping(target = Constants.DESCRIPTION,
      qualifiedByName = "blankStringToNull",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
  )
  void mapPhotoEntryUploadRequestToPhotoEntry(@MappingTarget PhotoEntry photoEntry,
      PhotoEntryUploadRequest photoEntryUploadRequest);

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
