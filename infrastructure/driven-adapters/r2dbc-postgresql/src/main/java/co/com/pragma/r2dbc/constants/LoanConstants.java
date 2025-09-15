package co.com.pragma.r2dbc.constants;



/**
 * Clase de constantes que centraliza los textos, logs y queries
 * usados en la persistencia de LoanType.
 *
 * Mantener las constantes separadas permite:
 * - Evitar duplicación de cadenas literales en el código.
 * - Facilitar la mantenibilidad y cambios en la base de datos.
 * - Mejorar la legibilidad del código.
 */
public final class LoanConstants {

    private LoanConstants() {
        throw new IllegalStateException("Utility class");
    }

    // Logs
    public static final String LOG_VERIFY_EXISTENCE = "Verificando existencia de LoanType con id={}";
    public static final String LOG_EXISTS_RESULT = "LoanType {}existe con id={}";
    public static final String LOG_VERIFY_ERROR = "Error verificando LoanType: {}";
    public static final String LOG_FOUND = "LoanType encontrado: {}";
    public static final String LOG_FIND_ERROR = "Error obteniendo LoanType: {}";

    // SQL
    public static final String SQL_EXISTS_BY_ID = """
        SELECT COUNT(*) AS total 
        FROM loan_schema.loan_type 
        WHERE id_loan_type = :id
        """;

    public static final String SQL_FIND_BY_ID = """
        SELECT id_loan_type, 
               name, 
               minimum_amount, 
               maximum_amount, 
               interest_rate, 
               automatic_validation
        FROM loan_schema.loan_type
        WHERE id_loan_type = :id
        """;
}
