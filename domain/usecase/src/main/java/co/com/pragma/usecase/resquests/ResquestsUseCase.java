package co.com.pragma.usecase.resquests;

import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResquestsUseCase {
    private final RequestsRepository requestsRepository;
    private final UserGateway userGateway; // 🔹 Se inyecta el gateway del API de usuarios

    public Mono<Requests> createLoanRequest(Requests requests) {
        // Validamos primero si el usuario existe
        return userGateway.existsUserByNoIdentification(requests.getIdentification())
                .flatMap(userExists -> {
                    if (Boolean.TRUE.equals(userExists)) {
                        // Si existe, guardamos la solicitud
                        return requestsRepository.createLoanRequest(requests);
                    } else {
                        // Si no existe, retornamos error
                        return Mono.error(new IllegalArgumentException("El usuario con ID " + requests.getIdentification() + " no existe."));
                    }
                });
    }
}
