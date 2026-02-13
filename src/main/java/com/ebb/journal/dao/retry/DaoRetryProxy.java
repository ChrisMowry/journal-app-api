package com.ebb.journal.dao.retry;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

@Component
public class DaoRetryProxy {

  @Retry(name = "serviceWriteOperation")
  public void executeRetryableDaoOperation(RetryableDaoOperation retryableDaoOperation) {
    retryableDaoOperation.execute();
  }

}
