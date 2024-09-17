package com.purpose.file_api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class FileStorageException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
}
