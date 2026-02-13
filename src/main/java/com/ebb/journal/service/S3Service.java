package com.ebb.journal.service;

import com.ebb.journal.configuration.AwsProperties;
import com.ebb.journal.exception.RetryableException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3Service {

  private final S3Client s3Client;
  private final S3AsyncClient s3AsyncClient;
  private final AwsProperties awsProperties;

  public S3Service(
      S3Client s3Client,
      S3AsyncClient s3AsyncClient,
      AwsProperties awsProperties) {
    this.s3Client = s3Client;
    this.s3AsyncClient = s3AsyncClient;
    this.awsProperties = awsProperties;
  }

  /**
   * Saves a file object to S3.
   *
   * @param key the file name to be saved to s3
   * @param data the file in a byte array
   * @param contentType the file type of the file
   */
  public void putObject(String key, byte[] data, String contentType) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(awsProperties.getS3().getBucketName())
            .key(key)
            .contentType(contentType)
            .build(),
        RequestBody.fromBytes(data)
    );
  }

  /**
   * Deletes an object in S3 asynchronously.
   *
   * @param objectKey s3 object key name (file name)
   */
  public void asyncDeleteObject(String objectKey) {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .bucket(awsProperties.getS3().getBucketName())
        .key(objectKey)
        .build();

    s3AsyncClient
        .deleteObject(request)
        .thenAccept(r -> log.info("Successfully deleted file {}", objectKey))
        .exceptionally(e -> {
          throw new RetryableException("Partial failure deleting S3 objects");
        });
  }

  /**
   * Batch deletes objects in S3 asynchronously.
   *
   * @param objectKeys a list of s3 object key names (file names)
   */
  public void asyncDeleteObjects(List<String> objectKeys) {
    if (objectKeys == null || objectKeys.isEmpty()) {
      CompletableFuture.completedFuture(null);
      return;
    }

    List<ObjectIdentifier> objects = objectKeys.stream()
        .map(k -> ObjectIdentifier.builder().key(k).build())
        .toList();

    DeleteObjectsRequest request = DeleteObjectsRequest.builder()
        .bucket(awsProperties.getS3().getBucketName())
        .delete(Delete.builder()
            .objects(objects)
            .build())
        .build();

    s3AsyncClient.deleteObjects(request)
        .thenApply(response -> {
          if (!response.errors().isEmpty()) {
            throw new RetryableException("Partial failure deleting S3 objects");
          }
          return null;
        });
  }
}