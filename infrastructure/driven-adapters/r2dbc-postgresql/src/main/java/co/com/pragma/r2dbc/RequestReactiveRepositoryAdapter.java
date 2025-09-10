
package co.com.pragma.r2dbc;

        import co.com.pragma.model.requests.Requests;
        import co.com.pragma.model.requests.gateways.RequestsRepository;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.r2dbc.core.DatabaseClient;
        import org.springframework.stereotype.Repository;
        import org.springframework.transaction.annotation.Transactional;
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

}
