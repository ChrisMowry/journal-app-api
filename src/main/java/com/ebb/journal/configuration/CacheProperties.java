package com.ebb.journal.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheProperties {
  private String jwkSigningKeysCacheSpecs;
  private String userCacheSpecs;
  private String journalEntryCacheSpecs;
  private String photoEntryCacheSpecs;
}
