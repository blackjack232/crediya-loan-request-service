package co.com.pragma.model.requests.constants;

import java.util.regex.Pattern;

public class RequestsLoanConstants {


    // Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Mensajes de error
    public static final String TOKEN_MISSING_OR_INVALID = "Token de autorización ausente o inválido";
    public static final String ERROR_PROCESSING_REQUEST = "No se pudieron obtener las solicitudes, intente nuevamente";
    public static final String GENERIC_ERROR_PREFIX = "Error: ";

    // Mensajes de éxito
    public static final String LOAN_CREATED_SUCCESS = "Solicitud de préstamo creada exitosamente";
    public static final String UPDATE_STATE_LOAN_REQUEST_SUCCESS = "Estado de la solicitud actualizado correctamente";
    public static final String IDENTIFICATION_MISSING = " Ingrese numero de identificacion";
    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Códigos de estado
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
}
