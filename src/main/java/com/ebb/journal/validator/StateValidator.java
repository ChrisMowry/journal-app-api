package com.ebb.journal.validator;

import static com.ebb.journal.util.Constants.STATES;
import static com.ebb.journal.util.Constants.STATE_REGEX;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StateValidator implements ConstraintValidator<ValidState, String> {

  /**
   * Implements the validation logic. The state of {@code value} must not be altered.
   * <p>
   * This method can be accessed concurrently, thread-safety must be ensured by the implementation.
   *
   * @param value   object to validate
   * @param context context in which the constraint is evaluated
   * @return {@code false} if {@code value} does not pass the constraint
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value.matches(STATE_REGEX) && STATES.contains(value);
  }
}
