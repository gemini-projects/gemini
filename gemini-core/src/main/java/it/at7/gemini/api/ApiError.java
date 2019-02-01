package it.at7.gemini.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.at7.gemini.core.Record;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

class ApiError {
    HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    LocalDateTime timestamp;
    String message;
    String errorcode;
    List<? extends Record> records;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    ApiError(HttpStatus status) {
        this();
        this.status = status;
    }

    ApiError(HttpStatus status, String errorCode, String message) {
        this(status);
        this.errorcode = errorCode;
        this.message = message;
    }

    ApiError(HttpStatus status, Record record) {
        this(status);
        this.records = List.of(record);
    }

    ApiError(HttpStatus status, List<? extends Record> records) {
        this(status);
        this.records = records;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public List<? extends Record> getRecords() {
        return records;
    }
}