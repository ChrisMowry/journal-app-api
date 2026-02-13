package com.ebb.journal.configuration;

import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt-util")
@Data
public class JwtUtilConfiguration {

  // how long jwks values will be cached for all authenticated requests
  private long authenticationJwksCacheTtlMillis;

  // how long jwks values will be cached in the upsert request cache
  private long upsertJwksCacheTtlMillis;

  // the refresh timeout of cached jwks
  private long upsertJwksCacheRefreshTimeoutMillis = JWKSourceBuilder.DEFAULT_CACHE_REFRESH_TIMEOUT; // 15 seconds

  // if jwks endpoint is non-responsive, will use existing cache this amount of time
  private long upsertJwksOutageCacheTtlMillis = 4 * 60 * 60 * 1000; // 4 hours
}
