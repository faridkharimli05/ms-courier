package az.delivery.mscourier.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourierNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public Map<String, String> handleNotFound(CourierNotFoundException ex) {
        return message(ex.getMessage());
    }

    @ExceptionHandler({CourierAlreadyExistsException.class, CourierAssignmentException.class})
    @ResponseStatus(CONFLICT)
    public Map<String, String> handleConflict(RuntimeException ex) {
        return message(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(CONFLICT)
    public Map<String, String> handleDataIntegrity(DataIntegrityViolationException ex) {
        return message("Courier data conflicts with existing data");
    }

    private Map<String, String> message(String message) {
        return Map.of("message", message);
    }
}
