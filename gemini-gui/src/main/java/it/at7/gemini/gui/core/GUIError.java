package it.at7.gemini.gui.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.at7.gemini.core.DynamicRecord;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public class GUIError {
    HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    LocalDateTime timestamp;
    String message;
    String errorcode;
    List<? extends DynamicRecord> records;

    private GUIError() {
        timestamp = LocalDateTime.now();
    }

    public GUIError(HttpStatus status) {
        this();
        this.status = status;
    }

    public GUIError(HttpStatus status, String errorCode, String message) {
        this(status);
        this.errorcode = errorCode;
        this.message = message;
    }

    public GUIError(HttpStatus status, DynamicRecord record) {
        this(status);
        this.records = List.of(record);
    }

    public GUIError(HttpStatus status, List<? extends DynamicRecord> records) {
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

    public List<? extends DynamicRecord> getRecords() {
        return records;
    }
}