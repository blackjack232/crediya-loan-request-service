package co.com.pragma.model.requests.constants;

public enum HttpCode {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500);

    private final int value;

    HttpCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

