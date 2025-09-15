package co.com.pragma.r2dbc;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.r2dbc.constants.LoanConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Repository
public class LoanReactiveRepositoryAdapter implements LoanTypeRepository {

    private final DatabaseClient client;

    public LoanReactiveRepositoryAdapter(DatabaseClient client) {
        this.client = client;
    }

    /**
     * Verifica si existe un LoanType en la base de datos dado su identificador.
     *
     * <p>
     * Ejecuta un <code>SELECT COUNT(*)</code> en la tabla
     * <code>loan_schema.loan_type</code> para determinar si existe un registro con
     * el id especificado. Devuelve un {@link Mono} booleano indicando la existencia.
     * </p>
     *
     * @param idLoanType Identificador del LoanType a verificar.
     * @return {@link Mono} que emite {@code true} si existe, {@code false} en caso contrario.
     *         Si ocurre un error en la consulta, se emite una excepción reactiva.
     */
    public Mono<Boolean> existsLoanTypeById(Long idLoanType) {
        log.info(LoanConstants.LOG_VERIFY_EXISTENCE, idLoanType);

        return client.sql(LoanConstants.SQL_EXISTS_BY_ID)
                .bind("id", idLoanType)
                .map(row -> row.get("total", Long.class))
                .one()
                .map(count -> count != null && count > 0)
                .doOnSuccess(exists -> log.info(
                        LoanConstants.LOG_EXISTS_RESULT, exists ? "" : "NO ", idLoanType))
                .doOnError(e -> log.error(LoanConstants.LOG_VERIFY_ERROR, e.getMessage(), e));
    }

    /**
     * Busca un LoanType en la base de datos por su identificador.
     *
     * <p>
     * Ejecuta un <code>SELECT</code> sobre la tabla
     * <code>loan_schema.loan_type</code> recuperando todos los atributos del tipo
     * de préstamo, y los mapea al modelo de dominio {@link LoanType}.
     * </p>
     *
     * @param idLoanType Identificador del LoanType a consultar.
     * @return {@link Mono} que emite la entidad {@link LoanType} encontrada. Si no se encuentra,
     *         emite un {@code Mono.empty()}. En caso de error se emite una excepción reactiva.
     */
    @Override
    public Mono<LoanType> findById(Long idLoanType) {
        return client.sql(LoanConstants.SQL_FIND_BY_ID)
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
                .doOnSuccess(lt -> log.info(LoanConstants.LOG_FOUND, lt))
                .doOnError(e -> log.error(LoanConstants.LOG_FIND_ERROR, e.getMessage(), e));
    }
}
