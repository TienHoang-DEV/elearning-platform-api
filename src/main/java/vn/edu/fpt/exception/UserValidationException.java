package vn.edu.fpt.exception;

public class UserValidationException extends RuntimeException{
    private String feild;

    public UserValidationException(String feild, String message) {
        super(message);
        this.feild = feild;
    }

    public String getFeild() {
        return feild;
    }
}
