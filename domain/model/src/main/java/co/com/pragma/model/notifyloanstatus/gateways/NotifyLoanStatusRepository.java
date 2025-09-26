package co.com.pragma.model.notifyloanstatus.gateways;

import reactor.core.publisher.Mono;

public interface NotifyLoanStatusRepository {
    /**
     * Envía un mensaje a SQS de manera asíncrona.
     *
     * @param message Mensaje en formato JSON que será enviado.
     * @return Mono con el MessageId de SQS si el envío fue exitoso.
     */
    Mono<String> send(String message);

}
