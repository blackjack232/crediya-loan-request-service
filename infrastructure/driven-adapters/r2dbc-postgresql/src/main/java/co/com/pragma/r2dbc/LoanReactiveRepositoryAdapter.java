
package co.com.pragma.r2dbc;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.r2dbc.entity.LoanTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Repository
public class LoanReactiveRepositoryAdapter implements LoanTypeRepository {

    private final DatabaseClient client;

    public LoanReactiveRepositoryAdapter(DatabaseClient client) {
        this.client = client;
    }

    public Mono<Boolean> existsLoanTypeById(Long idLoanType) {
        log.info("Verificando existencia de LoanType con id={}", idLoanType);

        return client.sql("""
                        SELECT COUNT(*) AS total 
                        FROM loan_schema.loan_type 
                        WHERE id_loan_type = :id
                        """)
                .bind("id", idLoanType)
                .map(row -> row.get("total", Long.class))
                .one()
                .map(count -> count != null && count > 0)
                .doOnSuccess(exists -> log.info("LoanType {}existe con id={}", exists ? "" : "NO ", idLoanType))
                .doOnError(e -> log.error("Error verificando LoanType: {}", e.getMessage(), e));
    }

    @Override
    public Mono<LoanType> findById(Long idLoanType) {
        return client.sql("""
                    SELECT id_loan_type, name, minimum_amount, maximum_amount, interest_rate, automatic_validation
                    FROM loan_schema.loan_type
                    WHERE id_loan_type = :id
                    """)
                .bind("id", idLoanType)
                .map(row -> LoanType.builder()
                        .idLoanType(row.get("id_loan_type", Long.class))
                        .name(row.get("name", String.class))
                        .minAmount(row.get("minimum_amount", BigDecimal.class))
                        .maxAmount(row.get("maximum_amount", BigDecimal.class))
                        .interestRate(row.get("interest_rate", Double.class))
                        .automaticValidation(row.get("automatic_validation", Boolean.class))
                        .build()
                )
                .one()
                .doOnSuccess(lt -> log.info("LoanType encontrado: {}", lt))
                .doOnError(e -> log.error("Error obteniendo LoanType: {}", e.getMessage(), e));
    }

}
