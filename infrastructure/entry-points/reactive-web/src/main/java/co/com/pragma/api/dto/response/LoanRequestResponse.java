package co.com.pragma.api.dto.response;

import java.math.BigDecimal;

public class LoanRequestResponse {

    private BigDecimal amount;
    private Integer term;
    private String email;
    private String identification;
    private String loanType;
    private BigDecimal interestRate;
    private String status; // "PENDIENTE_REVISION", "RECHAZADA", "REVISION_MANUAL"
    private BigDecimal totalMonthlyDebt;
}
