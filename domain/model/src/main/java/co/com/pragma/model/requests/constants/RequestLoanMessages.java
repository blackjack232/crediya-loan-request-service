package co.com.pragma.model.requests.constants;

/**
 * Clase que centraliza todos los mensajes utilizados en el caso de uso de solicitudes de préstamo.
 * <p>
 * Esto permite mantener los mensajes consistentes, fáciles de modificar y reutilizables
 * en diferentes partes de la aplicación.
 */
public final class RequestLoanMessages {

    private RequestLoanMessages() {
        throw new IllegalStateException("Utility class");
    }

    // Mensajes de validación
    public static final String INVALID_EMAIL = "El email no es válido";
    public static final String INVALID_AMOUNT = "El monto debe ser mayor a 0";
    public static final String INVALID_TERM = "El plazo debe estar entre 1 y 36 meses";

    // Mensajes relacionados con usuario
    public static final String USER_NOT_FOUND = "El usuario con identificación %s no existe o no tiene un token válido";

    // Mensajes relacionados con tipo de préstamo
    public static final String LOAN_TYPE_NOT_FOUND = "El tipo de préstamo con ID %s no existe.";
    public static final String INVALID_AMOUNT_RANGE =
            "El monto debe estar entre %s y %s para el préstamo %s";

    // Mensajes de logs o errores generales
    public static final String ERROR_FETCHING_REQUESTS = "Error al obtener solicitudes: %s";
    public static final String REQUESTS_FETCHED_OK = "Listado de solicitudes obtenido correctamente";
    public static final String USER_NOT_FOUND_OR_UNAUTHORIZED_ROLE =
            "Usuario no encontrado o rol no autorizado para realizar esta operación.";

    public static final String INVALID_STATUS = "Estado invalido";


    public static final String USER_NOT_AUTHORIZED = "El usuario no tiene rol de asesor.";




}
