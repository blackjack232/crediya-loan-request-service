package co.com.pragma.model.loantype.constants;

public class LoanMessages {

    private LoanMessages() {

    }

    public static final String FIRST_NAME_REQUIRED = "El nombre es requerido";
    public static final String LAST_NAME_REQUIRED = "El apellido es requerido";
    public static final String INVALID_EMAIL = "Formato de email invalido";
    public static final String BASE_SALARY_REQUIRED = "Salario fuera de rango (0 - 15.000.000)";
    public static final String EMAIL_ALREADY_EXISTS = "El email ya esta registrado";
    public static final String DOCUMENT_ALREADY_EXISTS = "El documento ya esta registrado";
    public static final String CREATE_LOAN_OK = "Prestamo creado exitosamente";
    public static final String ROLE_NOT_FOUND = "El rol especificado no existe";


    // Mensajes de error genérico
    public static final String ERROR_GENERAL = "Ocurrió un error inesperado";
}
