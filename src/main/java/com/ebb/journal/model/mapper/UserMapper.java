package com.ebb.journal.model.mapper;

import com.ebb.journal.model.User;
import com.ebb.journal.model.dto.UserUploadRequest;
import com.ebb.journal.util.Constants;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Maps a user with the user upload request.
   *
   * @param user the user to be updated
   * @param userUploadRequest the updates to be mapped
   */
  @BeanMapping(
      ignoreByDefault = true,
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
  )
  @Mapping(target = Constants.USER_ID, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.FIRST_NAME, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.LAST_NAME, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.EMAIL, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.PHONE_NUMBER, conditionQualifiedByName = "isNotBlank")
  @Mapping(target = Constants.BILLING_ADDRESS)
  @Mapping(target = Constants.DELIVERY_ADDRESS)
  void mapUserUploadRequestToUser(@MappingTarget User user, UserUploadRequest userUploadRequest);
}
