package com.ebb.journal.exception;

import static com.ebb.journal.util.Constants.TIMESTAMP;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  protected ResponseEntity<ProblemDetail> handleBadRequestException(
      BadRequestException ex, HttpServletRequest request) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    pd.setTitle("Bad Request");
    pd.setProperty(TIMESTAMP, Instant.now().toString());
    pd.setInstance(URI.create(request.getRequestURI()));

    log.warn("BadRequestException: {}", ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(pd);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ProblemDetail> handleNotFoundException(
      NotFoundException ex, HttpServletRequest request) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
    pd.setTitle("Not Found");
    pd.setProperty(TIMESTAMP, Instant.now().toString());
    pd.setInstance(URI.create(request.getRequestURI()));

    log.warn("NotFoundException: {}", ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(pd);
  }

  @ExceptionHandler(ServerErrorException.class)
  protected ResponseEntity<ProblemDetail> handleServerErrorException(
      ServerErrorException ex, HttpServletRequest request) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getLocalizedMessage());
    pd.setTitle("Internal Server Error");
    pd.setProperty(TIMESTAMP, Instant.now().toString());
    pd.setInstance(URI.create(request.getRequestURI()));

    log.warn("ServerErrorException: {}", ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(pd);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    pd.setTitle("Bad Request");
    pd.setProperty(TIMESTAMP, Instant.now().toString());
    pd.setInstance(URI.create(request.getRequestURI()));

    log.warn("ConstraintViolationException: {}", ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    pd.setTitle("Bad Request");
    pd.setProperty(TIMESTAMP, Instant.now().toString());
    pd.setInstance(URI.create(request.getRequestURI()));

    log.warn("MethodArgumentNotValidException: {}", ex.getLocalizedMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(pd);
  }
}
