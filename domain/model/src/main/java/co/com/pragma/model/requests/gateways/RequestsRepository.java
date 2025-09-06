package co.com.pragma.model.requests.gateways;

import co.com.pragma.model.requests.Requests;
import reactor.core.publisher.Mono;

public interface RequestsRepository {
    Mono<Requests> createLoanRequest(Requests requests);
}
