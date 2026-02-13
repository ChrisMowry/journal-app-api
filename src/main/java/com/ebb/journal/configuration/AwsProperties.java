package com.ebb.journal.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

  private String region;
  private boolean local = Boolean.FALSE;
  private DynamoDb dynamodb = new DynamoDb();
  private CloudFront cloudfront = new CloudFront();
  private S3 s3 = new S3();

  @Data
  public static class DynamoDb {
    private String tableName;
    private int clientExecutionTimeout;
    private String endpointUri;
  }

  @Data
  public static class CloudFront {
    private String keyPairId;
    private String privateKeySecretName;
    private String distributionDomain;
    private String privateKeyFilePath;
  }

  @Data
  public static class S3{
    private String bucketName;
  }
}