package co.com.pragma.model.user.gateways;
import co.com.pragma.model.requests.Requests;
import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Boolean> existsUserByNoIdentification(String identification , String authHeader);
    Mono<Boolean> verifyRole(String identification, String authHeader);

}
