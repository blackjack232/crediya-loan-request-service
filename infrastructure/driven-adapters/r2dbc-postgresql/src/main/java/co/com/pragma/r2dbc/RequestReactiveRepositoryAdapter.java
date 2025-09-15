package co.com.pragma.r2dbc;

import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.r2dbc.constants.RequestConstants;
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

    /**
     * Crea una nueva solicitud de préstamo en la base de datos.
     *
     * <p>
     * Inserta los datos del objeto {@link Requests} en la tabla
     * <code>loan_schema.request</code>, asignando por defecto el estado inicial
     * definido en {@link RequestConstants#DEFAULT_STATE_ID}. Una vez insertado,
     * se devuelve el mismo objeto con el campo <code>idRequest</code>
     * actualizado con el valor generado por la base de datos.
     * </p>
     *
     * @param requests Objeto con la información del préstamo a registrar.
     * @return {@link Mono} que emite el objeto {@link Requests} con el id asignado.
     *         En caso de error se emite una excepción reactiva.
     */
    @Override
    @Transactional
    public Mono<Requests> createLoanRequest(Requests requests) {
        log.warn(RequestConstants.LOG_INSERT_REQUEST, requests);

        return client.sql(RequestConstants.SQL_INSERT_REQUEST)
                .bind("amount", requests.getAmount())
                .bind("term", requests.getTerm())
                .bind("email", requests.getEmail())
                .bind("id_state", RequestConstants.DEFAULT_STATE_ID)
                .bind("id_loan_type", requests.getIdLoanType())
                .map(row -> requests.toBuilder()
                        .idRequest(row.get("id_request", Long.class))
                        .build())
                .one()
                .doOnSuccess(r -> log.info(RequestConstants.LOG_CREATED_REQUEST, r.getIdRequest()))
                .doOnError(e -> log.error(RequestConstants.LOG_INSERT_ERROR, e.getMessage(), e));
    }

    /**
     * Consulta las solicitudes de préstamo que están pendientes de revisión manual.
     *
     * <p>
     * Este método construye dinámicamente un query SQL que busca registros en
     * <code>loan_schema.request</code> cuyo estado corresponda a
     * {@link RequestConstants#PENDING_STATUS}. Adicionalmente, permite aplicar un
     * filtro opcional sobre el correo del solicitante. El resultado se devuelve de
     * manera paginada usando los parámetros <code>page</code> y <code>size</code>.
     * </p>
     *
     * @param page   Número de página (comenzando desde 0).
     * @param size   Tamaño de página, es decir, cantidad de registros a recuperar.
     * @param filter Texto opcional para filtrar por coincidencia parcial en el
     *               correo electrónico.
     * @return {@link Flux} que emite múltiples instancias de {@link Requests}. Si
     *         no hay resultados, el flujo estará vacío.
     */
    @Override
    public Flux<Requests> findRequestsForManualReview(int page, int size, String filter) {
        var spec = client.sql(RequestConstants.SQL_FIND_REQUESTS_FOR_MANUAL_REVIEW);

        if (filter != null && !filter.isBlank()) {
            spec = spec.bind("filter", filter);
        } else {
            spec = spec.bindNull("filter", String.class);
        }

        return spec.bind("size", size)
                .bind("offset", page * size)
                .map(row -> Requests.builder()
                        .idRequest(row.get("id_request", Long.class))
                        .amount(row.get("amount", java.math.BigDecimal.class))
                        .term(row.get("term", Integer.class))
                        .email(row.get("email", String.class))
                        .loanType(row.get("loan_type", String.class))
                        .interestRate(row.get("interest_rate", java.math.BigDecimal.class))
                        .status(row.get("status", String.class))
                        .build())
                .all()
                .doOnSubscribe(sub -> log.info(RequestConstants.LOG_QUERY_EXECUTION))
                .doOnError(e -> log.error(RequestConstants.LOG_QUERY_ERROR, e.getMessage(), e));
    }
}
