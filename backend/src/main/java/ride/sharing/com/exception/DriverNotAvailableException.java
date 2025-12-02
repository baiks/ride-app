package ride.sharing.com.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DriverNotAvailableException extends CustomException {
    public DriverNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}