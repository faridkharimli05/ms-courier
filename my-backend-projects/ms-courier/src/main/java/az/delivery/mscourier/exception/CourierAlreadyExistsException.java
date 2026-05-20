package az.delivery.mscourier.exception;

public class CourierAlreadyExistsException extends RuntimeException {

    public CourierAlreadyExistsException(String message) {
        super(message);
    }
}
