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

    private Long idRequest;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String identification;
    private Long idState;
    private Long idLoanType;
    private String loanType;
    private BigDecimal interestRate;
    private String status; // "PENDIENTE_REVISION", "RECHAZADA", "REVISION_MANUAL"
    private BigDecimal totalMonthlyDebt;
    private BigDecimal income;

}
