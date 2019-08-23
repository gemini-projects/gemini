package it.at7.gemini.gui.api;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.gui.core.GUIError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("it.at7.gemini.api")
public class GUIExceptionHandlerController {


    @ExceptionHandler(GeminiException.class)
    public ResponseEntity<?> handleRestoCheckedException(GeminiException exception) {
        return new ResponseEntity<>(new GUIError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getErrorCodeName(), exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        return new ResponseEntity<>(new GUIError(HttpStatus.INTERNAL_SERVER_ERROR, "CRITICAL_EXCEPTION", exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
