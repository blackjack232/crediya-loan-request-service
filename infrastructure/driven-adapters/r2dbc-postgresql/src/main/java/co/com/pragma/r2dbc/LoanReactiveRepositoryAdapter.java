
package co.com.pragma.r2dbc;

        import co.com.pragma.model.loantype.LoanType;
        import co.com.pragma.model.loantype.gateways.LoanTypeRepository;

        import lombok.extern.slf4j.Slf4j;
        import org.springframework.r2dbc.core.DatabaseClient;
        import org.springframework.stereotype.Repository;
        import org.springframework.transaction.annotation.Transactional;
        import reactor.core.publisher.Mono;
@Slf4j
@Repository
public class LoanReactiveRepositoryAdapter implements LoanTypeRepository {

    private final DatabaseClient client;

    public LoanReactiveRepositoryAdapter(DatabaseClient client) {
        this.client = client;
    }

    @Override
    @Transactional
    public Mono<LoanType> createLoan(LoanType loanType) {
        log.warn("Insertando LoanType en base de datos: {}", loanType.toString());

        return client.sql("""
            INSERT INTO loan_schema.loan_type (name, min_amount, max_amount, interest_rate, automatic_validation)
            VALUES (:name, :minAmount, :maxAmount, :interestRate, :automaticValidation)
            RETURNING id_loan_type
            """)
                .bind("name", loanType.getName())
                .bind("minAmount", loanType.getMinAmount())
                .bind("maxAmount", loanType.getMaxAmount())
                .bind("interestRate", loanType.getInterestRate())
                .bind("automaticValidation", loanType.getAutomaticValidation())
                .map(row -> loanType.toBuilder()
                        .idLoanType(row.get("id_loan_type", Long.class))
                        .build()
                )
                .one()
                .doOnSuccess(lt -> log.info("LoanType creado con id={}", lt.getIdLoanType()))
                .doOnError(e -> log.error("Error al insertar LoanType: {}", e.getMessage(), e));
    }


}
