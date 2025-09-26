package co.com.pragma.r2dbc.constants;

/**
 * Clase de constantes que centraliza todos los textos, queries SQL y mensajes
 * usados en la capa de persistencia para las solicitudes de préstamo.
 * <p>
 * Esto permite:
 * - Evitar duplicación de strings en el código.
 * - Hacer más fácil la mantenibilidad en caso de cambios en la base de datos.
 * - Facilitar pruebas y legibilidad del código.
 */
public final class RequestConstants {

    private RequestConstants() {
        throw new IllegalStateException("Utility class");
    }

    // Logs
    public static final String LOG_INSERT_REQUEST = "Insertando LoanRequest en base de datos: {}";
    public static final String LOG_CREATED_REQUEST = "LoanRequest creado con id={}";
    public static final String LOG_INSERT_ERROR = "Error al insertar LoanRequest: {}";
    public static final String LOG_QUERY_EXECUTION = "Ejecutando query para solicitudes pendientes...";
    public static final String LOG_QUERY_ERROR = "Error consultando solicitudes: {}";

    public static final String LOG_UPDATE_STATUS =
            "🔄 Actualizando estado de solicitud id={} → nuevo id_state={}";
    public static final String LOG_UPDATE_SUCCESS =
            "✅ Estado actualizado correctamente: id={}, nuevo id_state={}, nuevo status={}";
    public static final String LOG_UPDATE_NOT_FOUND =
            "⚠️ No se encontró ninguna solicitud con id={} para actualizar.";
    public static final String LOG_UPDATE_ERROR =
            "❌ Error al actualizar el estado de la solicitud id={}: {}";

    // Valores de negocio

    // Estados
    public static final int DEFAULT_STATE_ID = 1;
    public static final String PENDING_STATUS = "Pending";

    // Queries
    public static final String SQL_INSERT_REQUEST = """
            INSERT INTO loan_schema.request (amount, term, email, id_state, id_loan_type)
            VALUES (:amount, :term, :email, :id_state, :id_loan_type)
            RETURNING id_request
            """;

    public static final String SQL_FIND_REQUESTS_FOR_MANUAL_REVIEW = """
            SELECT r.id_request,
                   r.amount,
                   r.term,
                   r.email,
                   s.name AS status,
                   lt.name AS loan_type,
                   lt.interest_rate
            FROM loan_schema.request r
            JOIN loan_schema.states s ON r.id_state = s.id_state
            JOIN loan_schema.loan_type lt ON r.id_loan_type = lt.id_loan_type
            WHERE  s.name IN ('En espera')
            AND (:filter IS NULL OR r.email ILIKE '%' || :filter || '%')
            ORDER BY r.id_request DESC
            LIMIT :size OFFSET :offset
            """;

    public static final String SQL_UPDATE_LOAN_STATUS = """
            UPDATE loan_schema.request
            SET id_state = :id_state
            WHERE id_request = :id_request
            RETURNING id_request,
                      amount,
                      term,
                      email,
                      id_state,
                      (SELECT s.name
                         FROM loan_schema.states s
                        WHERE s.id_state = loan_schema.request.id_state) AS state_name
            """;

    public static final String SQL_FIND_APPROVED_LOANS_BY_EMAIL = """
    SELECT r.id_request, r.amount, r.term, lt.interest_rate
    FROM loan_schema.request r
    JOIN loan_schema.states s ON r.id_state = s.id_state
    JOIN loan_schema.loan_type lt ON r.id_loan_type = lt.id_loan_type
    WHERE r.email = :email AND s.name = 'Aprobada'
""";



    public static final String SQL_SELECT_REQUEST_BY_ID = """
    SELECT id_request, identification, amount, term, email, id_loan_type, /* otros campos */
    FROM loan_schema.request
    WHERE id_request = :idRequest
""";



}