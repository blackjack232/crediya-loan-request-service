package co.com.pragma.api;

import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.api.dto.request.UpdateLoanStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/loan-request",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createLoanRequest",
                    operation = @Operation(
                            operationId = "createLoanRequest",
                            summary = "Create a new loan request",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoanRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Loan created successfully",
                                            content = @Content(schema = @Schema(implementation = LoanRequest.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/get-requests",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getRequests",
                    operation = @Operation(
                            operationId = "getRequests",
                            summary = "Obtener listado de solicitudes para revision manual",
                            parameters = {
                                    @Parameter(
                                            name = "identification",
                                            description = "Numero de identificacion",
                                            required = true,
                                            schema = @Schema(type = "string", defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "page",
                                            description = "Numero de pagina",
                                            required = false,
                                            schema = @Schema(type = "integer", defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "size",
                                            description = "Tamano de pagina",
                                            required = false,
                                            schema = @Schema(type = "integer", defaultValue = "10")
                                    ),
                                    @Parameter(
                                            name = "filter",
                                            description = "Texto para filtrar por nombre o email",
                                            required = false,
                                            schema = @Schema(type = "string")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Listado de solicitudes para revision",
                                            content = @Content(schema = @Schema(implementation = co.com.pragma.model.requests.Requests.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/loan-request/state/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateLoanStatus",
                    operation = @Operation(
                            operationId = "updateLoanStatus",
                            summary = "Actualizar estado de la solicitud",
                            description = "Permite aprobar o rechazar una solicitud de préstamo",
                            parameters = {
                                    @Parameter(
                                            name = "identification",
                                            description = "Numero de identificacion",
                                            required = true,
                                            schema = @Schema(type = "string", defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "idLoanRequest",
                                            description = "ID de la solicitud",
                                            required = true,
                                            schema = @Schema(type = "long")
                                    ),
                                    @Parameter(
                                            name = "idState",
                                            description = "ID del estado",
                                            required = true,
                                            schema = @Schema(type = "long")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UpdateLoanStatusRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Estado de la solicitud actualizado correctamente"
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Usuario no autorizado"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/loan-request"), handler::createLoanRequest)
                .andRoute(GET("/api/get-requests"), handler::getRequests)
               .andRoute(PUT("/api/loan-request/state/{id}"), handler::updateLoanStatus);
    }
}
