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

import java.math.BigDecimal;

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
    /**
     * Actualiza el estado (id_state) de una solicitud de préstamo en la base de datos.
     *
     * <p>
     * Esta versión usa RETURNING con un JOIN a la tabla state para devolver
     * el nombre legible del estado en un solo roundtrip a la base de datos.
     * </p>
     *
     * @param requests Objeto que contiene el ID de la solicitud y el nuevo estado {@code idState}.
     * @return {@link Mono} que emite la solicitud actualizada si fue encontrada, o vacío si no existe.
     */
    @Override
    @Transactional
    public Mono<Requests> updateLoanStatus(Requests requests) {
        Long idRequest = requests.getIdRequest();
        Long idState = requests.getIdState();

        log.info(RequestConstants.LOG_UPDATE_STATUS, idRequest, idState);

        return client.sql(RequestConstants.SQL_UPDATE_LOAN_STATUS)
                .bind("id_state", idState)
                .bind("id_request", idRequest)
                .map(row -> Requests.builder()
                        .idRequest(row.get("id_request", Long.class))
                        .amount(row.get("amount", java.math.BigDecimal.class))
                        .term(row.get("term", Integer.class))
                        .email(row.get("email", String.class))
                        .idState(row.get("id_state", Long.class))
                        .status(row.get("state_name", String.class)) // ✅ Ahora trae el nombre del estado
                        .build())
                .one()
                .doOnSuccess(r -> {
                    if (r != null) {
                        log.info(RequestConstants.LOG_UPDATE_SUCCESS,
                                r.getIdRequest(), r.getIdState(), r.getStatus());
                    } else {
                        log.warn(RequestConstants.LOG_UPDATE_NOT_FOUND, idRequest);
                    }
                })
                .doOnError(e -> log.error(RequestConstants.LOG_UPDATE_ERROR, idRequest, e.getMessage(), e));
    }


    @Override
    public Flux<Requests> findApprovedLoansByUser(String email) {
        return client.sql(RequestConstants.SQL_FIND_APPROVED_LOANS_BY_EMAIL)
                .bind("email", email)
                .map(row -> Requests.builder()
                        .idRequest(row.get("id_request", Long.class))
                        .amount(row.get("amount", BigDecimal.class))
                        .term(row.get("term", Integer.class))
                        .interestRate(row.get("interest_rate", BigDecimal.class))
                        .build())
                .all()
                .doOnNext(request -> log.info("Solicitud encontrada: id={}, amount={}, term={}, interestRate={}",
                        request.getIdRequest(),
                        request.getAmount(),
                        request.getTerm(),
                        request.getInterestRate()
                ));
    }


}