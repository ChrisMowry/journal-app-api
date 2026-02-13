package com.ebb.journal.service;


import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.ebb.journal.configuration.AwsProperties;
import com.ebb.journal.exception.ServerErrorException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Service
public class CloudFrontService {

  private final SecretsManagerClient secretsClient;
  private final AwsProperties awsProperties;

  private volatile PrivateKey privateKey;

  public CloudFrontService(
      SecretsManagerClient secretsClient,
      AwsProperties awsProperties) {
    this.secretsClient = secretsClient;
    this.awsProperties = awsProperties;
  }

  @PostConstruct
  public void init() throws IOException {
    this.privateKey = loadPrivateKey();
  }

  /**
   * Generate a signed CloudFront URL for an object
   */
  public String generateSignedUrl(String objectKey, long ttlSeconds) {
    ensureKeyLoaded();

    String resourceUrl = String.format("https://%s/%s",
        this.awsProperties.getCloudfront().getDistributionDomain(), objectKey);

    Date expiresAt =
        Date.from(Instant.now().plusSeconds(ttlSeconds));

    return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
        resourceUrl,
        this.awsProperties.getCloudfront().getKeyPairId(),
        privateKey,
        expiresAt
    );
  }

  /**
   * Fetch private key from Secrets Manager
   */
  private PrivateKey loadPrivateKey()  {
    try {
      String pem = awsProperties.isLocal()
          ? Files.readString(Path.of(awsProperties.getCloudfront().getPrivateKeyFilePath()),
          Charset.defaultCharset())
          : secretsClient.getSecretValue(
              GetSecretValueRequest.builder()
                  .secretId(this.awsProperties.getCloudfront().getPrivateKeySecretName())
                  .build()
          ).secretString();

      return parsePrivateKey(pem);
    } catch (IOException ex){
      throw new ServerErrorException("Failed to load CloudFront private key", ex);
    }
  }

  /**
   * PEM → RSA PrivateKey (PKCS#8)
   */
  private PrivateKey parsePrivateKey(String pem) {
    try {
      String normalized = pem
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s", "");

      byte[] decoded = Base64.getDecoder().decode(normalized);

      PKCS8EncodedKeySpec spec =
          new PKCS8EncodedKeySpec(decoded);

      return KeyFactory.getInstance("RSA").generatePrivate(spec);

    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse CloudFront private key", e);
    }
  }

  private void ensureKeyLoaded(){
    if (privateKey == null) {
      synchronized (this) {
        if (privateKey == null) {
          privateKey = loadPrivateKey();
        }
      }
    }
  }
}
