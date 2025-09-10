package co.com.pragma.consumer;

import co.com.pragma.model.user.gateways.UserGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserGateway {
    private final WebClient client;

    @CircuitBreaker(name = "userService")
    @Override
    public Mono<Boolean> existsUserByNoIdentification(String identification, String authHeader) {
        return client
                .get()
                .uri("/api/users/{identification}", identification) // 🔹 pasas el número de identificación
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiUserResponse<Boolean>>() {})
                .map(ApiUserResponse::getData) // 🔹 obtenemos el campo `data`
                .doOnNext(result -> log.info("Respuesta de autenticación para identificación {}: {}", identification, result))
                .onErrorResume(e -> {
                    log.error("Error al consultar usuario {} en servicio de autenticación: {}", identification, e.getMessage(), e);
                    return Mono.just(false); // 🔹 devolvemos false en caso de error
                });
    }


// Possible fallback method
//    public Mono<String> testGetOk(Exception ignored) {
//        return client
//                .get() // TODO: change for another endpoint or destination
//                .retrieve()
//                .bodyToMono(String.class);
//    }
/*
    @CircuitBreaker(name = "testPost")
    public Mono<ObjectResponse> testPost() {
        ObjectRequest request = ObjectRequest.builder()
            .val1("exampleval1")
            .val2("exampleval2")
            .build();
        return client
                .post()
                .body(Mono.just(request), ObjectRequest.class)
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }

 */
}
