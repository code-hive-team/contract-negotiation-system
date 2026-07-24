package com.contractnegotiation.backend.exception;

import com.contractnegotiation.backend.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidFileException(InvalidFileException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<ApiResponseDto<String>> handleS3UploadException(S3UploadException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(S3DownloadException.class)
    public ResponseEntity<ApiResponseDto<String>> handleS3DownloadException(S3DownloadException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "S3 Download Failure: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FastApiException.class)
    public ResponseEntity<ApiResponseDto<String>> handleFastApiException(FastApiException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.BAD_GATEWAY.value(),
                "FastAPI Server Error: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(FastApiUnavailableException.class)
    public ResponseEntity<ApiResponseDto<String>> handleFastApiUnavailableException(FastApiUnavailableException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "FastAPI GenAI Server Unavailable: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<String>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<String>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied: You do not have permission to perform this action",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.BAD_REQUEST.value(),
                "File size exceeds maximum allowed limit",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponseDto<Map<String, String>> response = new ApiResponseDto<>(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleGlobalException(Exception ex) {
        ApiResponseDto<String> response = new ApiResponseDto<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
