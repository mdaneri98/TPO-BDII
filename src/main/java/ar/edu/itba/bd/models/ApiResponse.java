package ar.edu.itba.bd.models;

public record ApiResponse(String message, Object data) {
    
    public ApiResponse(String message) {
        this(message, null);
    }
    
    public ApiResponse(Object data) {
        this(null, data);
    }
}
