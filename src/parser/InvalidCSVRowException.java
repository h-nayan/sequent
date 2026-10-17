package parser;

public class InvalidCSVRowException extends RuntimeException{
    public InvalidCSVRowException(String message) {
        super(message);
    }
}