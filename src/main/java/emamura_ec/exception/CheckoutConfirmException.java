package emamura_ec.exception;

public class CheckoutConfirmException extends RuntimeException {

    private final String redirectPath;

    public CheckoutConfirmException(String message, String redirectPath) {
        super(message);
        this.redirectPath = redirectPath;
    }

    public String getRedirectPath() {
        return redirectPath;
    }
}
