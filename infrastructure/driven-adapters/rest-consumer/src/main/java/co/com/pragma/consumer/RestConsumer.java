package co.com.pragma.consumer;

import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.user.gateways.UserGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserGateway {
    private final WebClient client;

    @Value("${adapter.restconsumer.endpoints.exists-user}")
    private String existsUserEndpoint;



    @CircuitBreaker(name = "userService")
    @Override
    public Mono<Boolean> existsUserByNoIdentification(String identification, String authHeader) {
        return client
                .get()
                .uri(existsUserEndpoint, identification) // 🔹 pasas el número de identificación
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

    @Override
    public Mono<Boolean> verifyRole(String identification, String authHeader) {
        return client
                .get()
                .uri("/api/users/validate-role/{identification}", identification)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiUserResponse<Boolean>>() {})
                .map(response -> {
                    // ✅ Siempre usamos 'success' como fuente de verdad
                    boolean isAuthorized = Boolean.TRUE.equals(response.getData());
                    log.info("¿Usuario {} es asesor?: {}", identification, isAuthorized);
                    return isAuthorized;
                })
                .onErrorResume(e -> {
                    log.error("Error al verificar rol del usuario {}: {}", identification, e.getMessage(), e);
                    return Mono.just(false); // fallback seguro en caso de error
                });
    }






}
