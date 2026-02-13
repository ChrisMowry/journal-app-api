package com.ebb.journal.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@Slf4j
@EnableConfigurationProperties(AwsProperties.class)
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class AwsConfiguration {

  private final AwsProperties awsProperties;

  // Main table clients
  @Bean
  public DynamoDbEnhancedClient getDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
  }

  @Bean
  public DynamoDbClient getDynamoDBClient() throws URISyntaxException {
    if (this.awsProperties.getDynamodb().isLocal()) {
      return DynamoDbClient.builder()
          .endpointOverride(
              new URI(this.awsProperties.getDynamodb().getEndpointUri()))
          .credentialsProvider(() -> AwsBasicCredentials.create("dummy", "dummy"))
          .region(Region.of(this.awsProperties.getRegion()))
          .build();
    } else {
      return DynamoDbClient.builder().build();
    }
  }

  @Bean
  public S3Client s3Client(){
    return S3Client.builder()
        .region(Region.of(this.awsProperties.getRegion()))
        .build();
  }

  @Bean
  public S3AsyncClient s3AsyncClient() {
    return S3AsyncClient.builder()
        .region(Region.of(this.awsProperties.getRegion()))
        .build();
  }

  @Bean
  public SecretsManagerClient secretsManagerClient() {
    return SecretsManagerClient.builder()
        .region(Region.of(this.awsProperties.getRegion()))
        .build();
  }
}
