package com.purpose.file_api.exception;

import com.purpose.file_api.models.Error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class FileApiExceptionAdvice {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException exception) {
        List<String> details = new ArrayList<>();
        details.add(exception.getMessage());
        Error error = new Error(LocalDateTime.now(), "File Not Found", details);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException exception) {
        List<String> details = new ArrayList<>();
        details.add(exception.getMessage());
        Error error = new Error(LocalDateTime.now(), "File Storage Error", details);

        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE)
                .body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exception) {
        List<String> details = new ArrayList<>();
        details.add(exception.getMessage());
        Error error = new Error(LocalDateTime.now(), "File Size Exceeded", details);

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(error);
    }


}
