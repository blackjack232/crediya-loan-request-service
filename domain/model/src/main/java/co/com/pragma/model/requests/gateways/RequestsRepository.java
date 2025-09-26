package co.com.pragma.model.requests.gateways;

import co.com.pragma.model.loandecisionresult.PaymentInstallment;
import co.com.pragma.model.requests.Requests;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RequestsRepository {
    Mono<Requests> createLoanRequest(Requests requests);
    Flux<Requests> findRequestsForManualReview(int page, int size, String filter);
    Mono<Requests> updateLoanStatus(Requests request);
    Flux<Requests> findApprovedLoansByUser(String email);

}
