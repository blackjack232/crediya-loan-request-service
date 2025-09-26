package co.com.pragma.model.loantype;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {

    private Long idLoanType;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private Boolean automaticValidation;
}
