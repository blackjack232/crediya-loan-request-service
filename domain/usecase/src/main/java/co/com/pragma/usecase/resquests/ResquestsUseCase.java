package co.com.pragma.usecase.resquests;

import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.requests.Requests;
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

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Mono<Requests> createLoanRequest(Requests requests, String authHeader) {
        // Validaciones iniciales
        if (requests.getEmail() == null || !EMAIL_PATTERN.matcher(requests.getEmail()).matches()) {
            return Mono.error(new IllegalArgumentException("El email no es valido"));
        }
        if (requests.getAmount() == null || requests.getAmount().doubleValue() <= 0) {
            return Mono.error(new IllegalArgumentException("El monto debe ser mayor a 0"));
        }
        if (requests.getTerm() == null || requests.getTerm() < 1 || requests.getTerm() > 36) {
            return Mono.error(new IllegalArgumentException("El plazo debe estar entre 1 y 36 meses"));
        }

        // Paso 1: validar usuario
        return userGateway.existsUserByNoIdentification(requests.getIdentification(), authHeader)
                .flatMap(userExists -> {
                    if (Boolean.FALSE.equals(userExists)) {
                        return Mono.error(new IllegalArgumentException(
                                "El usuario con identificacion " + requests.getIdentification() + " no existe o no tiene un token valido"
                        ));
                    }

                    // Paso 2: obtener loanType
                    return loanTypeRepository.findById(requests.getIdLoanType())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "El tipo de prestamo con ID " + requests.getIdLoanType() + " no existe."
                            )))
                            .flatMap(loanType -> {
                                // Paso 3: validar monto contra min y max
                                if (requests.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                                        requests.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
                                    return Mono.error(new IllegalArgumentException(
                                            "El monto debe estar entre " + loanType.getMinAmount() +
                                                    " y " + loanType.getMaxAmount() +
                                                    " para el prestamo " + loanType.getName()
                                    ));
                                }

                                // Paso 4: guardar la solicitud
                                return requestsRepository.createLoanRequest(requests);
                            });
                });
    }
    public Flux<Requests> execute(int page, int size, String filter) {

        //log.info("Obteniendo solicitudes para revisión manual | page={}, size={}, filter={}", page, size, filter);
        return requestsRepository.findRequestsForManualReview(page, size, filter);
               // .doOnComplete(() -> log.info("Listado de solicitudes obtenido correctamente"))
               // .doOnError(e -> log.error("Error al obtener solicitudes: {}", e.getMessage(), e));
    }

}
