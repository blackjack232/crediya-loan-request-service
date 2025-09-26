package co.com.pragma.api;

import co.com.pragma.api.dto.request.CapacityTranslatorLoanRequest;
import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.api.dto.request.UpdateLoanStatusRequest;
import co.com.pragma.api.dto.response.LoanResponse;
import co.com.pragma.api.mapper.*;
import co.com.pragma.api.util.ResponseBuilder;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.constants.HttpCode;
import co.com.pragma.model.requests.constants.RequestsLoanConstants;
import co.com.pragma.model.requests.constants.UnauthorizedUserException;
import co.com.pragma.usecase.resquests.ResquestsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class Handler {

    private final ResquestsUseCase resquestsUseCase;
    private final LoanRequestMapper loanRequestMapper;
    private final CapacityTranslatorLoanRequestMapper capacityTranslatorLoanRequestMapper;
    private final LoanResponseMapper loanResponseMapper;
    private final UpdateLoanStatusMapper updateLoanStatusMapper;
    private final UpdateLoanStatusResponseMapper updateLoanStatusResponseMapper;

    /**
     * Endpoint encargado de crear una nueva solicitud de préstamo.
     *
     * <p>Flujo principal:
     * <ul>
     *   <li>Extrae el token JWT del encabezado {@code Authorization}.</li>
     *   <li>Valida el formato del token (prefijo {@code Bearer }).</li>
     *   <li>Convierte el cuerpo JSON en {@link LoanRequest} y lo mapea al dominio.</li>
     *   <li>Ejecuta el caso de uso {@link ResquestsUseCase#createLoanRequest}.</li>
     *   <li>Mapea la respuesta a {@link LoanResponse}.</li>
     *   <li>Construye la respuesta estándar de éxito con {@link ResponseBuilder}.</li>
     * </ul>
     *
     * <p>Errores controlados:
     * <ul>
     *   <li>Token ausente o inválido → {@code 401 Unauthorized}.</li>
     *   <li>Error en validaciones del caso de uso → {@code 400 Bad Request}.</li>
     * </ul>
     */
    public Mono<ServerResponse> createLoanRequest(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers()
                .firstHeader(RequestsLoanConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(RequestsLoanConstants.BEARER_PREFIX)) {
            return ResponseBuilder.error(
                    RequestsLoanConstants.TOKEN_MISSING_OR_INVALID,
                    HttpCode.UNAUTHORIZED.getValue()
            );
        }

        return serverRequest.bodyToMono(LoanRequest.class)
                .map(loanRequestMapper::toDomain)
                .flatMap(request -> resquestsUseCase.createLoanRequest(request, authHeader))
                .map(loanResponseMapper::toResponse)
                .flatMap(response -> ResponseBuilder.success(
                        response,
                        RequestsLoanConstants.LOAN_CREATED_SUCCESS,
                        HttpCode.CREATED.getValue()
                ))
                .onErrorResume(e -> ResponseBuilder.error(
                        RequestsLoanConstants.GENERIC_ERROR_PREFIX + e.getMessage(),
                        HttpCode.BAD_REQUEST.getValue()
                ));
    }

    /**
     * Endpoint encargado de obtener solicitudes de préstamo existentes de forma paginada y filtrada.
     *
     * <p>Flujo principal:
     * <ul>
     *   <li>Lee los parámetros de query: {@code page}, {@code size} y {@code filter}.</li>
     *   <li>Valida el token JWT en el encabezado {@code Authorization}.</li>
     *   <li>Ejecuta el caso de uso {@link ResquestsUseCase#execute} para obtener solicitudes.</li>
     *   <li>Devuelve la lista de solicitudes en JSON con código {@code 200 OK}.</li>
     * </ul>
     *
     * <p>Errores controlados:
     * <ul>
     *   <li>Token ausente o inválido → {@code 401 Unauthorized}.</li>
     *   <li>Error en ejecución del caso de uso → {@code 400 Bad Request}.</li>
     * </ul>
     */
    public Mono<ServerResponse> getRequests(ServerRequest serverRequest) {
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        String filter = serverRequest.queryParam("filter").orElse(null);
        String identification = serverRequest.queryParam("identification").orElse(null);

        String authHeader = serverRequest.headers()
                .firstHeader(RequestsLoanConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(RequestsLoanConstants.BEARER_PREFIX)) {
            return ResponseBuilder.error(
                    RequestsLoanConstants.TOKEN_MISSING_OR_INVALID,
                    HttpCode.UNAUTHORIZED.getValue()
            );
        }

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        resquestsUseCase.execute(page, size, filter, identification, authHeader)
                                .map(loanResponseMapper::toResponse),
                        LoanResponse.class
                )
                .onErrorResume(UnauthorizedUserException.class, ex -> {

                    return ResponseBuilder.error(
                            ex.getMessage(),
                            HttpCode.FORBIDDEN.getValue() // 403
                    );
                })
                .onErrorResume(e -> {

                    return ResponseBuilder.error(
                            RequestsLoanConstants.ERROR_PROCESSING_REQUEST,
                            HttpCode.BAD_REQUEST.getValue()
                    );
                });
    }

    /**
     * Endpoint encargado de actualizar el estado de una solicitud de préstamo.
     *
     * <p>Flujo principal:
     * <ul>
     *   <li>Extrae el token JWT del encabezado {@code Authorization}.</li>
     *   <li>Valida el formato del token (prefijo {@code Bearer }).</li>
     *   <li>Convierte el cuerpo JSON en {@link UpdateLoanStatusRequest} y lo mapea al dominio.</li>
     *   <li>Ejecuta el caso de uso {@link ResquestsUseCase#updateLoanStatus}.</li>
     *   <li>Construye la respuesta estándar de éxito con {@link ResponseBuilder}.</li>
     * </ul>
     *
     * <p>Errores controlados:
     * <ul>
     *   <li>Token ausente o inválido → {@code 401 Unauthorized}.</li>
     *   <li>Error en validaciones del caso de uso → {@code 400 Bad Request}.</li>
     * </ul>
     */
    public Mono<ServerResponse> updateLoanStatus(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers()
                .firstHeader(RequestsLoanConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(RequestsLoanConstants.BEARER_PREFIX)) {
            return ResponseBuilder.error(
                    RequestsLoanConstants.TOKEN_MISSING_OR_INVALID,
                    HttpCode.UNAUTHORIZED.getValue()
            );
        }

        return serverRequest.bodyToMono(UpdateLoanStatusRequest.class)
                .map(updateLoanStatusMapper::toDomain)  // 👈 mapeamos aquí
                .flatMap(request -> resquestsUseCase.updateLoanStatus(request, authHeader))
                .map(updateLoanStatusResponseMapper::toResponse)
                .flatMap(result -> ResponseBuilder.success(
                        result,
                        RequestsLoanConstants.UPDATE_STATE_LOAN_REQUEST_SUCCESS,
                        HttpCode.OK.getValue()
                ))
                .onErrorResume(e -> ResponseBuilder.error(
                        RequestsLoanConstants.GENERIC_ERROR_PREFIX + e.getMessage(),
                        HttpCode.BAD_REQUEST.getValue()
                ));
    }

    /**
     * Endpoint to calculate the applicant's available borrowing capacity.
     *
     * <p>Main flow:
     * <ul>
     *   <li>Validates JWT token from {@code Authorization} header.</li>
     *   <li>Reads request body and maps it to domain model.</li>
     *   <li>Calls use case {@link ResquestsUseCase#calculateBorrowingCapacity}.</li>
     *   <li>Returns decision (APPROVED, REJECTED, MANUAL_REVIEW) in the response.</li>
     * </ul>
     *
     * <p>Error handling:
     * <ul>
     *   <li>Invalid token → {@code 401 Unauthorized}.</li>
     *   <li>Validation errors → {@code 400 Bad Request}.</li>
     * </ul>
     */
    public Mono<ServerResponse> calculateCapacity(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers()
                .firstHeader(RequestsLoanConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(RequestsLoanConstants.BEARER_PREFIX)) {
            return ResponseBuilder.error(
                    RequestsLoanConstants.TOKEN_MISSING_OR_INVALID,
                    HttpCode.UNAUTHORIZED.getValue()
            );
        }

        return serverRequest.bodyToMono(CapacityTranslatorLoanRequest.class)
                .map(capacityTranslatorLoanRequestMapper::toDomain)
                .flatMap(request ->
                        resquestsUseCase.calculateBorrowingCapacity(request, authHeader)
                                .flatMap(result ->
                                        resquestsUseCase.processBorrowingCapacityAndNotify(request, result)
                                                .then(ResponseBuilder.success(
                                                        result,
                                                        RequestsLoanConstants.CAPACITY_CALCULATION_SUCCESS,
                                                        HttpCode.OK.getValue()
                                                ))
                                )
                )
                .onErrorResume(e ->
                        ResponseBuilder.error(
                                RequestsLoanConstants.GENERIC_ERROR_PREFIX + e.getMessage(),
                                HttpCode.BAD_REQUEST.getValue()
                        )
                );
    }




}
