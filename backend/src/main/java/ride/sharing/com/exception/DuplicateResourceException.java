package ride.sharing.com.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateResourceException extends CustomException {
    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value), 
              HttpStatus.CONFLICT);
    }
    
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}