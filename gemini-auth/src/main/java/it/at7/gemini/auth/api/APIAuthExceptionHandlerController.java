package it.at7.gemini.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice({"it.at7.gemini.auth"})
public class APIAuthExceptionHandlerController {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleEntityFoundException(BadCredentialsException exception) {
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(exception.getMessage(), httpStatus);
    }

}
