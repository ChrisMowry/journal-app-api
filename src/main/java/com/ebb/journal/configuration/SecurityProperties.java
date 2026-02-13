package com.ebb.journal.configuration;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("authentication")
public class SecurityProperties {
  private List<String> issuerUris;
  private String jwkSetUri;
  private int jwksRequestTimeout;
  private int jwksConnectTimeout;
  private int jwksCacheTtl;
  private int jwksCacheRefreshTimeout;
  private int jwksCacheSizeLimit;
  private int jwksOutageToleranceTtl;
  private List<String> audiences;
  private List<String> jwsAlgorithms;
  private boolean jwksRetryEnabled;
  private boolean jwksRateLimiterEnabled;
}
