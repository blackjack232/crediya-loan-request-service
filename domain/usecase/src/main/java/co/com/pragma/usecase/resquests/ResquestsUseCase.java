package co.com.pragma.usecase.resquests;

import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.constants.RequestLoanMessages;
import co.com.pragma.model.requests.constants.RequestsLoanConstants;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ResquestsUseCase {
    private final RequestsRepository requestsRepository;
    private final UserGateway userGateway;
    private final LoanTypeRepository loanTypeRepository;


    /**
     * Caso de uso para crear una nueva solicitud de préstamo en el sistema.
     *
     * <p>Flujo principal:
     * <ul>
     *   <li>Valida los datos básicos de la solicitud: email, monto y plazo.</li>
     *   <li>Verifica que el usuario asociado a la solicitud exista en el sistema,
     *       validando su identificación y el token de autorización.</li>
     *   <li>Consulta el tipo de préstamo (LoanType) para validar que exista.</li>
     *   <li>Verifica que el monto solicitado esté dentro del rango mínimo y máximo permitido
     *       para ese tipo de préstamo.</li>
     *   <li>Si todas las validaciones pasan, se guarda la solicitud en el repositorio.</li>
     * </ul>
     *
     * <p>Flujos alternativos (errores):
     * <ul>
     *   <li>Email inválido → {@code INVALID_EMAIL}.</li>
     *   <li>Monto inválido → {@code INVALID_AMOUNT}.</li>
     *   <li>Plazo fuera de rango → {@code INVALID_TERM}.</li>
     *   <li>Usuario no encontrado → {@code USER_NOT_FOUND}.</li>
     *   <li>Tipo de préstamo no existe → {@code LOAN_TYPE_NOT_FOUND}.</li>
     *   <li>Monto fuera de rango permitido → {@code INVALID_AMOUNT_RANGE}.</li>
     * </ul>
     *
     * @param requests   Solicitud de préstamo con los datos a validar y guardar.
     * @param authHeader Encabezado de autorización con token JWT del usuario solicitante.
     * @return {@link Mono} que emite la solicitud creada si todas las validaciones pasan,
     *         o un error si alguna validación falla.
     */
    public Mono<Requests> createLoanRequest(Requests requests, String authHeader) {
        // 1. Validar campos iniciales
        validateInitialRequestData(requests);

        // 2. Validar existencia de usuario
        return validateUserExists(requests, authHeader)
                .flatMap(userExists -> validateLoanTypeAndAmount(requests));
    }

    /**
     * Valida los datos iniciales de la solicitud (email, monto, plazo).
     *
     * @param requests Solicitud de préstamo a validar.
     */
    private void validateInitialRequestData(Requests requests) {
        if (requests.getEmail() == null || !RequestsLoanConstants.EMAIL_PATTERN.matcher(requests.getEmail()).matches()) {
            throw new IllegalArgumentException(RequestLoanMessages.INVALID_EMAIL);
        }
        if (requests.getAmount() == null || requests.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException(RequestLoanMessages.INVALID_AMOUNT);
        }
        if (requests.getTerm() == null || requests.getTerm() < 1 || requests.getTerm() > 36) {
            throw new IllegalArgumentException(RequestLoanMessages.INVALID_TERM);
        }
    }

    /**
     * Verifica si el usuario existe en el sistema a partir de su identificación y token JWT.
     *
     * @param requests   Solicitud que contiene la identificación del usuario.
     * @param authHeader Token JWT del usuario solicitante.
     * @return {@link Mono} que emite {@code true} si el usuario existe, o error si no existe.
     */
    private Mono<Boolean> validateUserExists(Requests requests, String authHeader) {
        return userGateway.existsUserByNoIdentification(requests.getIdentification(), authHeader)
                .flatMap(userExists -> {
                    if (Boolean.FALSE.equals(userExists)) {
                        return Mono.error(new IllegalArgumentException(
                                String.format(RequestLoanMessages.USER_NOT_FOUND, requests.getIdentification())
                        ));
                    }
                    return Mono.just(true);
                });
    }

    /**
     * Valida que el tipo de préstamo exista y que el monto solicitado esté dentro del rango permitido.
     * Si todo es correcto, guarda la solicitud en el repositorio.
     *
     * @param requests Solicitud con los datos a validar.
     * @return {@link Mono} que emite la solicitud creada o un error si falla alguna validación.
     */
    private Mono<Requests> validateLoanTypeAndAmount(Requests requests) {
        return loanTypeRepository.findById(requests.getIdLoanType())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        String.format(RequestLoanMessages.LOAN_TYPE_NOT_FOUND, requests.getIdLoanType())
                )))
                .flatMap(loanType -> {
                    if (requests.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                            requests.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
                        return Mono.error(new IllegalArgumentException(
                                String.format(RequestLoanMessages.INVALID_AMOUNT_RANGE,
                                        loanType.getMinAmount(),
                                        loanType.getMaxAmount(),
                                        loanType.getName())
                        ));
                    }
                    return requestsRepository.createLoanRequest(requests);
                });
    }

    /**
     * Obtiene un listado paginado de solicitudes de préstamo que requieren revisión manual.
     *
     * <p>Flujo principal:
     * <ul>
     *   <li>Recibe los parámetros de paginación: página, tamaño y un filtro opcional.</li>
     *   <li>Delegamos la consulta al {@link RequestsRepository} que devuelve un {@link Flux} de solicitudes.</li>
     *   <li>Permite procesar los resultados de manera reactiva, ideal para grandes volúmenes de datos.</li>
     * </ul>
     *
     * <p>Flujos alternativos (errores):
     * <ul>
     *   <li>Si ocurre un error en la consulta a la base de datos, el {@link Flux} emite un error con mensaje
     *       {@code ERROR_FETCHING_REQUESTS}.</li>
     * </ul>
     *
     * @param page   Número de página a consultar (por defecto 0).
     * @param size   Cantidad de registros por página (por defecto 10).
     * @param filter Filtro opcional aplicado a los resultados.
     * @return {@link Flux} que emite la lista de {@link Requests}, o un error si ocurre un problema.
     */
    public Flux<Requests> execute(int page, int size, String filter) {
        return requestsRepository.findRequestsForManualReview(page, size, filter);
    }
}
