package co.com.pragma.usecase.resquests;

import co.com.pragma.model.loandecisionresult.LoanDecisionResult;
import co.com.pragma.model.loandecisionresult.PaymentInstallment;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.notifyloanstatus.gateways.NotifyLoanStatusRepository;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.constants.RequestLoanMessages;
import co.com.pragma.model.requests.constants.RequestsLoanConstants;
import co.com.pragma.model.requests.constants.UnauthorizedUserException;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ResquestsUseCase {
    private final RequestsRepository requestsRepository;
    private final UserGateway userGateway;
    private final LoanTypeRepository loanTypeRepository;
    private final NotifyLoanStatusRepository sqsSender;

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
     * o un error si alguna validación falla.
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
    public Mono<Requests> validateLoanTypeAndAmount(Requests requests) {
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
     *   <li>Verifica que el usuario exista y tenga rol autorizado mediante {@link UserGateway}.</li>
     *   <li>Si es autorizado, delega la consulta al {@link RequestsRepository} que devuelve un {@link Flux} de solicitudes.</li>
     * </ul>
     *
     * <p>Flujos alternativos (errores):
     * <ul>
     *   <li>Si el usuario no está autorizado, se emite un error con mensaje {@code USER_NOT_AUTHORIZED}.</li>
     *   <li>Si ocurre un error en la consulta, el error se propagará automáticamente.</li>
     * </ul>
     *
     * @param page           Número de página a consultar (por defecto 0).
     * @param size           Cantidad de registros por página (por defecto 10).
     * @param filter         Filtro opcional aplicado a los resultados.
     * @param identification Número de identificación del usuario solicitante.
     * @param authHeader     Token de autorización.
     * @return {@link Flux} que emite la lista de {@link Requests}, o un error si el usuario no está autorizado o falla la consulta.
     */
    public Flux<Requests> execute(int page, int size, String filter, String identification, String authHeader) {
        return userGateway.verifyRole(identification, authHeader)
                .flatMapMany(roleExists -> {
                    if (Boolean.FALSE.equals(roleExists)) {

                        return Flux.error(new UnauthorizedUserException(RequestLoanMessages.USER_NOT_AUTHORIZED));
                    }
                    return requestsRepository.findRequestsForManualReview(page, size, filter);
                });
    }

    /**
     * Caso de uso: Actualiza el estado de una solicitud de préstamo existente y envía
     * una notificación a SQS con los datos actualizados.
     */
    public Mono<Requests> updateLoanStatus(Requests request, String authHeader) {
        if (request.getIdRequest() == null || request.getIdState() == null) {
            return Mono.error(new IllegalArgumentException(RequestLoanMessages.INVALID_REQUEST_OR_STATE));
        }
        if (request.getIdentification() == null || request.getIdentification().isBlank()) {
            return Mono.error(new IllegalArgumentException(RequestLoanMessages.IDENTIFICATION_REQUIRED));
        }
        if (authHeader == null || !authHeader.startsWith(RequestsLoanConstants.BEARER_PREFIX)) {
            return Mono.error(new SecurityException(RequestLoanMessages.TOKEN_MISSING_OR_INVALID));
        }

        return userGateway.verifyRole(request.getIdentification(), authHeader)
                .flatMap(isAuthorized -> {
                    if (!isAuthorized) {
                        return Mono.error(new SecurityException(RequestLoanMessages.USER_NOT_AUTHORIZED));
                    }

                    // 1️⃣ Intentamos actualizar en la BD
                    return requestsRepository.updateLoanStatus(request)
                            .flatMap(updatedRequest -> {
                                if (updatedRequest == null) {
                                    // No se encontró o no se actualizó la solicitud
                                    return Mono.error(new IllegalStateException(RequestLoanMessages.REQUEST_NOT_FOUND));
                                }

                                // 2️⃣ Si todo sale bien, enviamos el mensaje a SQS
                                String message = String.format(
                                        RequestsLoanConstants.SQS_MESSAGE_TEMPLATE,
                                        updatedRequest.getIdRequest(),
                                        updatedRequest.getStatus(),
                                        updatedRequest.getAmount() != null ? updatedRequest.getAmount() : 0.00
                                );

                                return sqsSender.send(message)
                                        .thenReturn(updatedRequest); // Retornamos la solicitud actualizada
                            });
                });
    }

    /**
     * Calcula la capacidad de endeudamiento disponible de un solicitante y devuelve
     * un resultado con decisión, cuota mensual, capacidad disponible, deuda actual
     * y plan de pagos.
     */
    public Mono<LoanDecisionResult> calculateBorrowingCapacity(Requests request, String authHeader) {
        validateInitialRequestData(request);

        return validateUserExists(request, authHeader)
                .flatMap(userExists -> loanTypeRepository.findById(request.getIdLoanType())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                String.format(RequestLoanMessages.LOAN_TYPE_NOT_FOUND, request.getIdLoanType())
                        )))
                        .flatMap(loanType ->
                                requestsRepository.findApprovedLoansByUser(request.getEmail())
                                        .collectList()

                                        .map(loans -> {
                                            // 1️⃣ Capacidad máxima (35% de ingresos)
                                            BigDecimal ingresosTotales = request.getIncome() != null
                                                    ? request.getIncome()
                                                    : BigDecimal.ZERO;
                                            BigDecimal capacidadMax = ingresosTotales.multiply(BigDecimal.valueOf(0.35));

                                            // 2️⃣ Deuda mensual actual
                                            BigDecimal deudaActual = loans.stream()
                                                    .map(l -> calculateMonthlyPayment(
                                                            l.getAmount(),
                                                            l.getInterestRate(),
                                                            l.getTerm()
                                                    ))
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                                            // 3️⃣ Capacidad disponible
                                            BigDecimal capacidadDisponible = capacidadMax.subtract(deudaActual);

                                            // 4️⃣ Cuota del nuevo préstamo
                                            BigDecimal cuotaNuevo = calculateMonthlyPayment(
                                                    request.getAmount(),
                                                    loanType.getInterestRate(),
                                                    request.getTerm()
                                            );

                                            // 5️⃣ Decisión
                                            String decision;
                                            if (cuotaNuevo.compareTo(capacidadDisponible) <= 0) {
                                                decision = request.getAmount().compareTo(
                                                        ingresosTotales.multiply(BigDecimal.valueOf(5))
                                                ) > 0 ? "MANUAL_REVIEW" : "APPROVED";
                                            } else {
                                                decision = "REJECTED";
                                            }

                                            // 6️⃣ Generar plan de pagos
                                            List<PaymentInstallment> paymentPlan = generatePaymentPlan(
                                                    request.getAmount(),
                                                    loanType.getInterestRate(),
                                                    request.getTerm()
                                            );

                                            return
                                                    LoanDecisionResult.builder()
                                                    .decision(decision)
                                                    .monthlyInstallment(cuotaNuevo)
                                                    .availableCapacity(capacidadDisponible)
                                                    .debtLoad(deudaActual)
                                                    .paymentPlan(paymentPlan)
                                                    .build();
                                        })
                        ));
    }


    /**
     * Calcula la cuota mensual de un préstamo usando la fórmula de amortización
     * francesa (cuota fija) considerando capital, tasa anual y plazo en meses.
     *
     * @param principal  Monto del préstamo.
     * @param annualRate Tasa de interés anual en porcentaje (ej. 12 para 12%).
     * @param months     Plazo del préstamo en meses.
     * @return La cuota mensual redondeada a 2 decimales.
     * @throws IllegalArgumentException Si el número de meses es menor o igual a cero.
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int months) {
        if (months <= 0) throw new IllegalArgumentException("months must be > 0");
        double P = principal.doubleValue();
        double i = annualRate.doubleValue() / 12.0 / 100.0;
        if (i == 0.0) {
            return BigDecimal.valueOf(P / months).setScale(2, RoundingMode.HALF_UP);
        }
        double payment = (P * i) / (1 - Math.pow(1 + i, -months));
        return BigDecimal.valueOf(payment).setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * Genera el plan de pagos mensual de un préstamo incluyendo:
     * número de cuota, abono a capital, pago de intereses y saldo restante.
     *
     * @param amount Monto del préstamo.
     * @param rate   Tasa de interés anual en porcentaje (ej. 12 para 12%).
     * @param term   Plazo del préstamo en meses.
     * @return Lista de PaymentInstallment representando cada cuota del plan.
     */
    private List<PaymentInstallment> generatePaymentPlan(BigDecimal amount, BigDecimal rate, int term) {
        List<PaymentInstallment> plan = new ArrayList<>();
        BigDecimal remaining = amount;
        BigDecimal monthlyRate = rate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP) // pasar a decimal
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP); // dividir en meses

        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, rate, term);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remaining.multiply(monthlyRate);
            BigDecimal capitalPayment = monthlyPayment.subtract(interestPayment);
            remaining = remaining.subtract(capitalPayment);

            plan.add(new PaymentInstallment(
                    i,
                    capitalPayment.setScale(2, RoundingMode.HALF_UP),
                    interestPayment.setScale(2, RoundingMode.HALF_UP),
                    remaining.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return plan;
    }


    public Mono<Void> processBorrowingCapacityAndNotify(Requests request, LoanDecisionResult result) {
        String emailContent = buildSqsMessage(request, result);

        return sqsSender.send(emailContent)
                .onErrorResume(ex -> Mono.empty()) // ignora cualquier error al enviar
                .then();
    }
/*

    /**
     * Metodo que Construye el contenido del email con el plan de pagos detallado.

    private String buildEmailContent(Requests request, LoanDecisionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola ").append(request.getIdentification()).append(",\n\n");
        sb.append("Su solicitud de préstamo ha sido evaluada. Resultado: ").append(result.getDecision()).append("\n");
        sb.append("Cuota mensual: ").append(result.getMonthlyInstallment()).append("\n");
        sb.append("Capacidad disponible: ").append(result.getAvailableCapacity()).append("\n\n");
        sb.append("Plan de pagos:\n");

        for (PaymentInstallment pi : result.getPaymentPlan()) {
            sb.append(String.format("Cuota %d: Capital %.2f, Interés %.2f, Saldo restante %.2f\n",
                    pi.getNumber(),
                    pi.getCapitalPayment(),
                    pi.getInterestPayment(),
                    pi.getRemainingBalance()));
        }

        sb.append("\nGracias por usar nuestro sistema.");
        return sb.toString();
    }
    */
    /**
     * Construye el mensaje SQS con todos los detalles de la solicitud y plan de pagos.
     */
    private String buildSqsMessage(Requests request, LoanDecisionResult result) {
        StringBuilder sb = new StringBuilder();

        // Información principal
        sb.append(String.format("Solicitud ID: %d\n", request.getIdRequest()));
        sb.append(String.format("Estado: %s\n", result.getDecision()));
        sb.append(String.format("Monto solicitado: %.2f\n", request.getAmount() != null ? request.getAmount() : 0.00));
        sb.append(String.format("Cuota mensual: %.2f\n", result.getMonthlyInstallment()));
        sb.append(String.format("Capacidad disponible: %.2f\n", result.getAvailableCapacity()));
        sb.append(String.format("Deuda actual: %.2f\n\n", result.getDebtLoad()));

        // Plan de pagos
        sb.append("Plan de pagos:\n");
        for (PaymentInstallment pi : result.getPaymentPlan()) {
            sb.append(String.format("Cuota %d: Capital %.2f, Interés %.2f, Saldo restante %.2f\n",
                    pi.getNumber(),
                    pi.getCapitalPayment(),
                    pi.getInterestPayment(),
                    pi.getRemainingBalance()));
        }

        sb.append("\nGracias por usar nuestro sistema.");
        return sb.toString();
    }



}
