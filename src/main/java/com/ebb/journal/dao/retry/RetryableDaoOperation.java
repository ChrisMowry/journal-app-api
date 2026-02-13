package com.ebb.journal.dao.retry;

import com.ebb.journal.exception.RetryableException;

@FunctionalInterface
public interface RetryableDaoOperation {

  void execute() throws RetryableException;
}
