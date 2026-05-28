package re.edu.hackathon.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private final int statusCode;
    private final String message;
    private final T data;
    private final Object error;
    private final LocalDateTime timestamp;

    private ApiResponse(int statusCode, String message, T data, Object error) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data, null);
    }

    public static ApiResponse<Object> error(int statusCode, String message, Object error) {
        return new ApiResponse<>(statusCode, message, null, error);
    }

}
