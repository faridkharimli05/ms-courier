package az.delivery.mscourier.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourierNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCourierNotFound(CourierNotFoundException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CourierAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleCourierAlreadyExists(CourierAlreadyExistsException ex) {
        return ResponseEntity
                .status(CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CourierAssignmentException.class)
    public ResponseEntity<Map<String, String>> handleCourierAssignment(CourierAssignmentException ex) {
        return ResponseEntity
                .status(CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(CONFLICT)
                .body(Map.of("message", "Courier data conflicts with existing data"));
    }
}
