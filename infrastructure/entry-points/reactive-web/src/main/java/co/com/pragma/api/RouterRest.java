package co.com.pragma.api;

import co.com.pragma.api.dto.request.CapacityTranslatorLoanRequest;
import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.api.dto.request.UpdateLoanStatusRequest;
import co.com.pragma.api.mapper.CapacityTranslatorLoanRequestMapper;
import co.com.pragma.model.loandecisionresult.LoanDecisionResult;
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
                            summary = "Crear una nueva solicitud de préstamo",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoanRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud creada correctamente",
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
                            summary = "Obtener listado de solicitudes para revisión manual",
                            parameters = {
                                    @Parameter(
                                            name = "identification",
                                            description = "Número de identificación",
                                            required = true,
                                            schema = @Schema(type = "string")
                                    ),
                                    @Parameter(
                                            name = "page",
                                            description = "Número de página",
                                            schema = @Schema(type = "integer", defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "size",
                                            description = "Tamaño de página",
                                            schema = @Schema(type = "integer", defaultValue = "10")
                                    ),
                                    @Parameter(
                                            name = "filter",
                                            description = "Filtro por email o nombre",
                                            schema = @Schema(type = "string")
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/loan-request/state",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateLoanStatus",
                    operation = @Operation(
                            operationId = "updateLoanStatus",
                            summary = "Actualizar el estado de una solicitud de préstamo",
                            description = "Permite aprobar o rechazar una solicitud",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = UpdateLoanStatusRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
                                    @ApiResponse(responseCode = "401", description = "Usuario no autorizado"),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
                            }
                    )
            ),

            // 🚀 NUEVA RUTA PARA CAPACIDAD DE ENDEUDAMIENTO
            @RouterOperation(
                    path = "/api/v1/calculate-capacity",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "calculateCapacity",
                    operation = @Operation(
                            operationId = "calculateCapacity",
                            summary = "Calcular la capacidad de endeudamiento disponible",
                            description = "Calcula si el solicitante puede asumir un nuevo préstamo y responde con una decisión (APPROVED, REJECTED, MANUAL_REVIEW).",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CapacityTranslatorLoanRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Resultado del análisis de capacidad",
                                            content = @Content(schema = @Schema(implementation = LoanDecisionResult.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "401", description = "Token inválido o faltante")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/loan-request"), handler::createLoanRequest)
                .andRoute(GET("/api/get-requests"), handler::getRequests)
                .andRoute(PUT("/api/loan-request/state"), handler::updateLoanStatus)
                // 🚀 NUEVA RUTA REGISTRADA EN EL ROUTER
                .andRoute(POST("/api/v1/calculate-capacity"), handler::calculateCapacity);
    }
}

