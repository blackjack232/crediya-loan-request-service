package co.com.pragma.usecase.loan;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanUseCase {

    private final LoanTypeRepository loanTypeRepository;
    public Mono<LoanType> createLoan(LoanType loan) {
        return loanTypeRepository.createLoan(loan);
    }

}
