package co.com.pragma.api.util;

import co.com.pragma.api.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ResponseBuilder {

    private ResponseBuilder() {}

    // Respuesta exitosa con datos
    public static <T> Mono<ServerResponse> success(T data, String message, int code) {
        return ServerResponse.status(code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.<T>builder()
                        .message(message)
                        .code(code)
                        .success(true)
                        .data(data)
                        .build());
    }

    // Respuesta de error con mensaje
    public static Mono<ServerResponse> error(String message, int code) {
        return ServerResponse.status(code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ApiResponse.<Object>builder()
                        .message(message)
                        .code(code)
                        .success(false)
                        .data(null)
                        .build());
    }
}
