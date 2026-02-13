package com.ebb.journal.model.dto;

import com.ebb.journal.validator.ValidJpeg;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
public class PhotoEntryUploadRequest {

  @NotNull
  @NonNull
  @Min(value = 1, message = "photoId must be a positive integer")
  @Max(value = 3, message = "photoId must be no greater than 3")
  private Integer photoId;

  @Size(max = 1000, message = "Photo captions must be less than 1000 characters")
  private String description;

  @NotNull
  @NonNull
  @ValidJpeg
  private MultipartFile photo;
}
