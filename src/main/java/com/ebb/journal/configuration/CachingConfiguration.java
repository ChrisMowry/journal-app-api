package com.ebb.journal.configuration;

import static com.ebb.journal.util.Constants.DATA_CACHE_MANAGER;
import static com.ebb.journal.util.Constants.JOURNAL_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.Constants.JWKS_CACHE_MANAGER;
import static com.ebb.journal.util.Constants.JWK_SIGNING_KEYS_CACHE_NAME;
import static com.ebb.journal.util.Constants.PHOTO_ENTRY_CACHE_NAME;
import static com.ebb.journal.util.Constants.USER_CACHE_NAME;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CachingConfiguration {

  @Primary
  @Bean(name = JWKS_CACHE_MANAGER)
  public CacheManager jwksCacheManager(CacheProperties cacheProperties) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    CaffeineSpec spec = CaffeineSpec.parse(cacheProperties.getJwkSigningKeysCacheSpecs());
    cacheManager.registerCustomCache(
        JWK_SIGNING_KEYS_CACHE_NAME, Caffeine.from(spec).recordStats().build());
    return cacheManager;
  }

  @Bean(name = DATA_CACHE_MANAGER)
  public CacheManager cacheManager(CacheProperties cacheProperties) {
    SimpleCacheManager manager = new SimpleCacheManager();

    CaffeineCache usersCache =
        new CaffeineCache(
            USER_CACHE_NAME,
            Caffeine.from(cacheProperties.getUserCacheSpecs()).build()
        );

    CaffeineCache journalEntryCache =
        new CaffeineCache(
            JOURNAL_ENTRY_CACHE_NAME,
            Caffeine.from(cacheProperties.getJournalEntryCacheSpecs()).build()
        );

    CaffeineCache photoEntryCache =
        new CaffeineCache(
            PHOTO_ENTRY_CACHE_NAME,
            Caffeine.from(cacheProperties.getPhotoEntryCacheSpecs()).build()
        );

    manager.setCaches(List.of(usersCache, journalEntryCache, photoEntryCache));
    return manager;
  }
}
