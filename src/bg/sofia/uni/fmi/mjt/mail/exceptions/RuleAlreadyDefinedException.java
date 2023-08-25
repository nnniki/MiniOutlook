package bg.sofia.uni.fmi.mjt.mail.exceptions;

public class RuleAlreadyDefinedException extends RuntimeException {
    public RuleAlreadyDefinedException(String message) {
        super(message);
    }

    public RuleAlreadyDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
