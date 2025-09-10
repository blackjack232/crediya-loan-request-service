
package co.com.pragma.r2dbc;

        import co.com.pragma.model.requests.Requests;
        import co.com.pragma.model.requests.gateways.RequestsRepository;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.r2dbc.core.DatabaseClient;
        import org.springframework.stereotype.Repository;
        import org.springframework.transaction.annotation.Transactional;
        import reactor.core.publisher.Flux;
        import reactor.core.publisher.Mono;
@Slf4j
@Repository
public class RequestReactiveRepositoryAdapter implements RequestsRepository {

    private final DatabaseClient client;

    public RequestReactiveRepositoryAdapter(DatabaseClient client) {
        this.client = client;
    }

    @Override
    @Transactional
    public Mono<Requests> createLoanRequest(Requests requests) {
        log.warn("Insertando LoanRequest en base de datos: {}", requests);

        return client.sql("""
                        INSERT INTO loan_schema.request (amount, term, email, id_state, id_loan_type)
                        VALUES (:amount, :term, :email, :id_state, :id_loan_type)
                        RETURNING id_request
                        """)
                .bind("amount", requests.getAmount())
                .bind("term", requests.getTerm())
                .bind("email", requests.getEmail())
                .bind("id_state", 1)
                .bind("id_loan_type", requests.getId_loan_type())
                .map(row -> requests.toBuilder()
                        .id_request(row.get("id_request", Long.class))
                        .build()
                )
                .one()
                .doOnSuccess(r -> log.info("LoanRequest creado con id={}", r.getId_request()))
                .doOnError(e -> log.error("Error al insertar LoanRequest: {}", e.getMessage(), e));

    }

    @Override
    public Flux<Requests> findRequestsForManualReview(int page, int size, String filter) {
        String sql = """
    SELECT r.id_request,
           r.amount,
           r.term,
           r.email,
           s.name AS state,
           lt.name AS loan_type,
           lt.interest_rate
    FROM loan_schema.request r
    JOIN loan_schema.states s ON r.id_state = s.id_state
    JOIN loan_schema.loan_type lt ON r.id_loan_type = lt.id_loan_type
    WHERE s.name IN ('PENDIENTE_REVISION','RECHAZADA','REVISION_MANUAL')
    AND (:filter IS NULL OR r.email ILIKE '%' || :filter || '%')
    ORDER BY r.id_request DESC
    LIMIT :size OFFSET :offset
    """;


        var spec = client.sql(sql);

        if (filter != null && !filter.isBlank()) {
            spec = spec.bind("filter", filter);
        } else {
            spec = spec.bindNull("filter", String.class); // 👈 aquí está la clave
        }

        return spec.bind("size", size)
                .bind("offset", page * size)
                .map(row -> Requests.builder()
                        .id_request(row.get("id_request", Long.class))
                        .amount(row.get("amount", java.math.BigDecimal.class))
                        .term(row.get("term", Integer.class))
                        .email(row.get("email", String.class))
                        .customerName(row.get("customer_name", String.class))
                        .loanType(row.get("loan_type", String.class))
                        .interestRate(row.get("interest_rate", java.math.BigDecimal.class))
                        .status(row.get("status", String.class))
                        .baseSalary(row.get("base_salary", java.math.BigDecimal.class))
                        .totalMonthlyDebt(row.get("total_monthly_debt", java.math.BigDecimal.class))
                        .build())
                .all()
                .doOnSubscribe(sub -> log.info("Ejecutando query para solicitudes pendientes..."))
                .doOnError(e -> log.error("Error consultando solicitudes: {}", e.getMessage(), e));
    }


}
