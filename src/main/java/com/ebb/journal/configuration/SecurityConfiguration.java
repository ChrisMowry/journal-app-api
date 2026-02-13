package com.ebb.journal.configuration;

import static com.ebb.journal.util.Constants.JWK_SIGNING_KEYS_CACHE_NAME;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final AuthenticationEntryPoint jwtAuthEntryPoint;
  private final SecurityProperties securityProperties;
  private final CacheManager cacheManager;

  private static final String AUTHENTICATED_ENDPOINTS = "/authenticated/**";
  private static final List<String> UNAUTHENTICATED_ENDPOINTS =
      List.of("/swagger*", "/actuator/**", "/v3/api-docs*", "/public/**");

  public SecurityConfiguration(
      AuthenticationEntryPoint jwtAuthEntryPoint,
      SecurityProperties securityProperties,
      CacheManager cacheManager) {
    this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    this.securityProperties = securityProperties;
    this.cacheManager = cacheManager;
  }

  /**
   * Creates an OAuth2TokenValidator with default validations along with custom validations. The
   * default validation includes... - Empty token check - X509CertificateThumbprintValidator -
   * JwtTimestampValidator
   *
   * @param validators list of OAuth2TokenValidator<Jwt> validators to use to verfy a JWT
   * @return OAuth2TokenValidator with default and custom validations
   */
  public OAuth2TokenValidator<Jwt> getAccessTokenOauth2TokenValidator(
      List<OAuth2TokenValidator<Jwt>> validators) {
    return JwtValidators.createDefaultWithValidators(validators);
  }

  /**
   * This method returns a validator that check the audience of a claim against the list of valid
   * audiences defined in the security properties.
   *
   * @return JwtClaimValidator with valid audiences
   */
  protected OAuth2TokenValidator<Jwt> getTokenOauth2AudienceValidator(List<String> audiences) {
    if (Objects.isNull(audiences) || audiences.isEmpty()) {
      throw new RuntimeException("Audience list cannot be null or empty");
    }
    return new JwtClaimValidator<List<String>>(
        OAuth2TokenIntrospectionClaimNames.AUD,
        aud ->
            CollectionUtils.containsAny(
                aud.stream().map(String::toLowerCase).toList(),
                audiences.stream().map(String::toLowerCase).toList()));
  }

  /**
   * Create an instance of JwtIssuerAuthenticationManagerResolver for authenticated users.
   *
   * @param securityProperties      - security properties
   * @param authenticationConverter - auth converter
   * @return JwtIssuerAuthenticationManagerResolver
   */
  @Bean("authenticatedResolver")
  public JwtIssuerAuthenticationManagerResolver authenticationManagerResolver(
      SecurityProperties securityProperties, JwtAuthenticationConverter authenticationConverter) {
    return createJwtIssuerAuthenticationManagerResolver(
        authenticationConverter, securityProperties.getJwkSetUri());
  }

  /**
   * Create an instance of JwtIssuerAuthenticationManagerResolver based on issuer passed. The
   * default validation can be found in the org.springframework.security.oauth2.jwt.
   *
   * @param authenticationConverter - auth converter
   * @param jwkSetUri               - URI for the jwk public keys
   * @return JwtIssuerAuthenticationManagerResolver
   */
  private JwtIssuerAuthenticationManagerResolver createJwtIssuerAuthenticationManagerResolver(
      JwtAuthenticationConverter authenticationConverter, String jwkSetUri) {
    return new JwtIssuerAuthenticationManagerResolver(
        (issuer) -> {
          // this will properly fail validation when the right jwt is not passed for the right path
          if (jwkSetUri == null) {
            return null;
          }

          NimbusJwtDecoder jwtDecoder =
              NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                  .jwsAlgorithms(
                      algorithms ->
                          this.securityProperties
                              .getJwsAlgorithms()
                              .forEach(
                                  algorithm -> algorithms.add(SignatureAlgorithm.from(algorithm))))
                  .cache(Objects.requireNonNull(cacheManager.getCache(JWK_SIGNING_KEYS_CACHE_NAME)))
                  .build();
          List<OAuth2TokenValidator<Jwt>> validators =
              List.of(
                  new JwtIssuerValidator(issuer),
                  getTokenOauth2AudienceValidator(securityProperties.getAudiences()));
          jwtDecoder.setJwtValidator(this.getAccessTokenOauth2TokenValidator(validators));
          JwtAuthenticationProvider authenticationProvider =
              new JwtAuthenticationProvider(jwtDecoder);

          authenticationProvider.setJwtAuthenticationConverter(authenticationConverter);
          return authenticationProvider::authenticate;
        });
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  // Unsecured Endpoint Security
  public SecurityFilterChain unsecuredFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable);
    http.sessionManagement(
        sessionMgt -> sessionMgt.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.securityMatcher(UNAUTHENTICATED_ENDPOINTS.toArray(new String[0]));
    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  // JWT Authenticated Endpoint Security
  public SecurityFilterChain jwtFilterChain(
      HttpSecurity http,
      @Qualifier("authenticatedResolver")
      JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable);
    http.sessionManagement(
        sessionMgt -> sessionMgt.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Verify JWT with OAuth2.
    http.securityMatcher(AUTHENTICATED_ENDPOINTS)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth2ResourceServer ->
                oauth2ResourceServer
                    .authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver)
                    .authenticationEntryPoint(jwtAuthEntryPoint));

    return http.build();
  }

  /**
   * Authentication converter to be used by the JwtIssuerAuthenticationManagerResolver
   *
   * @return JwtAuthenticationConverter
   */
  @Bean
  protected JwtAuthenticationConverter authenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
    return converter;
  }
}
