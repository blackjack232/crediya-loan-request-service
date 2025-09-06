package co.com.pragma.api;

import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.api.dto.response.ApiResponse;
import co.com.pragma.api.dto.response.LoanResponse;
import co.com.pragma.api.mapper.LoanRequestMapper;
import co.com.pragma.api.mapper.LoanResponseMapper;
import co.com.pragma.model.loantype.constants.LoanMessages;
import co.com.pragma.usecase.resquests.ResquestsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final LoanResponseMapper loanResponseMapper;
    public Mono<ServerResponse> createLoanRequest(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanRequest.class)
                .map(loanRequestMapper::toDomain)  // DTO → Domain
                .flatMap(resquestsUseCase::createLoanRequest)
                .map(loanResponseMapper::toResponse) // Domain → DTO
                .flatMap(createLoanRequest -> {
                    ApiResponse<LoanResponse> response = ApiResponse.<LoanResponse>builder()
                            .message(LoanMessages.CREATE_LOAN_OK)
                            .code(201)
                            .success(true)
                            .data(createLoanRequest)
                            .build();

                    return ServerResponse.status(HttpStatus.CREATED) // 201
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(e -> {
                    ApiResponse<Object> errorResponse = ApiResponse.builder()
                            .message("Error: " + e.getMessage())
                            .code(400)
                            .success(false)
                            .data(null)
                            .build();

                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }
}
