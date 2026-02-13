package com.ebb.journal.configuration;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

@Configuration
public class ErrorMessagingConfiguration {

  // Used to change Spring's default error response to look like our
  // ErrorResponse object. This affects all error messages that can't
  // be caught using the RestExceptionHandler or custom AuthenticationEndpoints
  @Bean
  public ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      public @NonNull Map<String, Object> getErrorAttributes(@NonNull WebRequest webRequest,
          @NonNull ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest,
            options.including(ErrorAttributeOptions.Include.STACK_TRACE));
        // Customize the default entries in errorAttributes to suit your needs
        errorAttributes.remove("path");
        errorAttributes.remove("timestamp");
        errorAttributes.remove("trace");
        errorAttributes.put("errorCode", errorAttributes.get("status"));
        errorAttributes.put("errorMessage", errorAttributes.get("error"));
        errorAttributes.remove("error");
        errorAttributes.remove("message");
        errorAttributes.remove("status");
        return errorAttributes;
      }
    };
  }
}
