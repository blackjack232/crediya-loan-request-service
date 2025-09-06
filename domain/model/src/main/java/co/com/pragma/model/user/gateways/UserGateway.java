package co.com.pragma.model.user.gateways;
import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Boolean> existsUserByNoIdentification(String identification);
}
