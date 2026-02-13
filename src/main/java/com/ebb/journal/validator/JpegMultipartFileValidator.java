package com.ebb.journal.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class JpegMultipartFileValidator
    implements ConstraintValidator<ValidJpeg, MultipartFile> {

  private static final long MAX_SIZE = 5242880; // 5MB
  // JPEG magic bytes: FF D8 FF
  private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file == null || file.isEmpty() || file.getSize() > MAX_SIZE) {
      return false;
    }

    // Check MIME type (cheap, but not sufficient alone)
    String contentType = file.getContentType();
    if (!"image/jpeg".equalsIgnoreCase(contentType)) {
      return false;
    }

    // Check magic bytes (real validation)
    try (InputStream is = file.getInputStream()) {
      byte[] header = new byte[3];
      if (is.read(header) != 3) {
        return false;
      }

      for (int i = 0; i < JPEG_MAGIC.length; i++) {
        if (header[i] != JPEG_MAGIC[i]) {
          return false;
        }
      }

      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
