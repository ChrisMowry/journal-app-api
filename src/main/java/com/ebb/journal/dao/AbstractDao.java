package com.ebb.journal.dao;

import com.ebb.journal.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Slf4j
public abstract class AbstractDao<T> {

  protected final DynamoDbEnhancedClient dynamoDbEnhancedClient;
  protected final DynamoDbTable<T> table;
  private final static String PK_SK_EXIST_EXPRESSION = "attribute_exists(#pk) AND attribute_exists(#sk)";
  private final static String PLACEHOLDER_PK_VALUE = "#pk";
  private final static String PLACEHOLDER_SK_VALUE = "#sk";
  private final static String PK_FIELD_NAME = "pk";
  private final static String SK_FIELD_NAME = "sk";

  // Builds the condition that checks if the partition and sort values exist in the DynamoDB Database.
  private final static Expression CONDITION = Expression.builder()
      .expression(PK_SK_EXIST_EXPRESSION)
      .expressionNames(
          Map.of(PLACEHOLDER_PK_VALUE, PK_FIELD_NAME, PLACEHOLDER_SK_VALUE, SK_FIELD_NAME))
      .build();

  public AbstractDao(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName,
      Class<T> clazz) {
    this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(clazz));
  }

  /**
   * Get a record by partition key and sort key.
   *
   * @param partitionValue       partition key value
   * @param sortValue            sort key value
   * @param isStronglyConsistent whether to use strongly consistent read
   * @return an Optional containing the record if found, or empty if not found
   */
  public Optional<T> getRecordByPkAndSk(String partitionValue, String sortValue,
      boolean isStronglyConsistent) {
    // build the query key
    Key queryKey = Key.builder().partitionValue(partitionValue).sortValue(sortValue).build();
    // define the query request
    QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
        .queryConditional(QueryConditional.keyEqualTo(queryKey))
        .consistentRead(isStronglyConsistent)
        .build();
    // query the table
    PageIterable<T> pageIterable = this.table.query(queryEnhancedRequest);

    // return the first item in the query results
    return Optional.ofNullable(pageIterable.items().stream().toList().getFirst());
  }

  /**
   * Get a list of records by partition key and sort key prefix.
   *
   * @param partitionValue       partition key value
   * @param sortPrefixValue      sort key prefix value
   * @param isStronglyConsistent whether to use strongly consistent read
   * @return a list of records matching the criteria
   */
  public List<T> getRecordsByPkAndSkPrefix(String partitionValue, String sortPrefixValue,
      boolean isStronglyConsistent) {
    // build the query key
    Key queryKey = Key.builder().partitionValue(partitionValue).sortValue(sortPrefixValue).build();
    // define the query conditional
    QueryConditional queryConditional = QueryConditional.sortBeginsWith(queryKey);
    // build the enhanced request
    QueryEnhancedRequest queryEnhancedRequest =
        QueryEnhancedRequest.builder().queryConditional(queryConditional)
            .consistentRead(isStronglyConsistent).build();
    // query the table
    PageIterable<T> pageIterable = this.table.query(queryEnhancedRequest);

    // return the list of query results
    return pageIterable.items().stream().toList();
  }

  /**
   * Put a record into the DynamoDB table.
   *
   * @param record the record to be added or updated
   */
  public void putRecord(T record) {
    this.table.putItem(record);
  }

  /**
   * Delete a record by partition key and sort key.
   *
   * @param partitionValue the partition key value
   * @param sortValue the sort key value
   */
  public void deleteRecordByPkAndSk(String partitionValue, String sortValue) {
    Key key = Key.builder()
        .partitionValue(partitionValue)
        .sortValue(sortValue)
        .build();
    DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder()
        .key(key)
        .conditionExpression(CONDITION)
        .build();
    try {
      this.table.deleteItem(deleteItemEnhancedRequest);
    } catch (ConditionalCheckFailedException ex) {
      String message = String.format("Record with pk %s and sk %s not found.",
          key.partitionKeyValue(), key.sortKeyValue());
      log.warn(message);
      throw new NotFoundException(message);
    }
  }
}
