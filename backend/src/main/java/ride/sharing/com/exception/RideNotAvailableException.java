package ride.sharing.com.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RideNotAvailableException extends CustomException {
    public RideNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}