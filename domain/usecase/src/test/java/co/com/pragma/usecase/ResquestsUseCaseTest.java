
package co.com.pragma.usecase;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.notifyloanstatus.gateways.NotifyLoanStatusRepository;
import co.com.pragma.model.requests.Requests;
import co.com.pragma.model.requests.constants.RequestLoanMessages;
import co.com.pragma.model.requests.constants.RequestsLoanConstants;
import co.com.pragma.model.requests.gateways.RequestsRepository;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.resquests.ResquestsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResquestsUseCaseTest {

    @Mock
    RequestsRepository requestsRepository;
    @Mock
    UserGateway userGateway;
    @Mock
    LoanTypeRepository loanTypeRepository;
    @Mock
    NotifyLoanStatusRepository sqsSender;

    @InjectMocks
    ResquestsUseCase useCase;

    private Requests sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new Requests();
        sampleRequest.setEmail("test@example.com");
        sampleRequest.setAmount(BigDecimal.valueOf(5000));
        sampleRequest.setTerm(12);
        sampleRequest.setIdentification("123");
        sampleRequest.setIdLoanType(10L);
    }

    @Test
    void createLoanRequest_shouldCreateSuccessfully() {
        // Arrange
        when(userGateway.existsUserByNoIdentification("123", "Bearer token"))
                .thenReturn(Mono.just(true));

        LoanType loanType = new LoanType();
        loanType.setMinAmount(BigDecimal.valueOf(1000));
        loanType.setMaxAmount(BigDecimal.valueOf(10000));
        loanType.setName("Personal");

        when(loanTypeRepository.findById(10L)).thenReturn(Mono.just(loanType));
        when(requestsRepository.createLoanRequest(sampleRequest)).thenReturn(Mono.just(sampleRequest));

        // Act & Assert
        StepVerifier.create(useCase.createLoanRequest(sampleRequest, "Bearer token"))
                .expectNext(sampleRequest)
                .verifyComplete();

        verify(requestsRepository).createLoanRequest(sampleRequest);
    }



    @Test
    void execute_shouldReturnFluxWhenUserAuthorized() {
        when(userGateway.verifyRole("123", "Bearer token")).thenReturn(Mono.just(true));
        when(requestsRepository.findRequestsForManualReview(0, 10, ""))
                .thenReturn(Flux.just(sampleRequest));

        StepVerifier.create(useCase.execute(0, 10, "", "123", "Bearer token"))
                .expectNext(sampleRequest)
                .verifyComplete();

        verify(requestsRepository).findRequestsForManualReview(0, 10, "");
    }

    @Test
    void execute_shouldReturnErrorWhenUnauthorized() {
        when(userGateway.verifyRole("123", "Bearer token")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.execute(0, 10, "", "123", "Bearer token"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().contains(RequestLoanMessages.USER_NOT_AUTHORIZED))
                .verify();

        verify(requestsRepository, never()).findRequestsForManualReview(anyInt(), anyInt(), any());
    }

    @Test
    void updateLoanStatus_shouldUpdateAndSendSqs() {
        sampleRequest.setIdRequest(10L);
        sampleRequest.setIdState(1L);
        sampleRequest.setStatus("APPROVED");

        when(userGateway.verifyRole("123", "Bearer token")).thenReturn(Mono.just(true));
        when(requestsRepository.updateLoanStatus(sampleRequest)).thenReturn(Mono.just(sampleRequest));
        when(sqsSender.send(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateLoanStatus(sampleRequest, "Bearer token"))
                .expectNext(sampleRequest)
                .verifyComplete();

        verify(sqsSender).send(anyString());
    }

    @Test
    void updateLoanStatus_shouldFailWhenUnauthorized() {
        sampleRequest.setIdRequest(10L);
        sampleRequest.setIdState(1L);

        when(userGateway.verifyRole("123", "Bearer token")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.updateLoanStatus(sampleRequest, "Bearer token"))
                .expectError(SecurityException.class)
                .verify();

        verify(requestsRepository, never()).updateLoanStatus(any());
    }
}
