package co.com.pragma.model.loantype.gateways;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.requests.Requests;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<Boolean> existsLoanTypeById(Long id);
    Mono<LoanType> findById(Long idLoanType);
}
