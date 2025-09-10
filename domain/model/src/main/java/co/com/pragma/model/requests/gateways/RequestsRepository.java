package co.com.pragma.model.requests.gateways;

import co.com.pragma.model.requests.Requests;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RequestsRepository {
    Mono<Requests> createLoanRequest(Requests requests);
    Flux<Requests> findRequestsForManualReview(int page, int size, String filter);
}
