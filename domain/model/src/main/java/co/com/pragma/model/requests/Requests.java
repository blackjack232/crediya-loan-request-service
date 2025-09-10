package co.com.pragma.model.requests;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Requests {

    private Long id_request;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String identification;
    private Long id_state;
    private Long id_loan_type;
    private String customerName;
    private String loanType;
    private BigDecimal interestRate;
    private String status; // "PENDIENTE_REVISION", "RECHAZADA", "REVISION_MANUAL"
    private BigDecimal baseSalary;
    private BigDecimal totalMonthlyDebt;

}
