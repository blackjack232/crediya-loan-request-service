package co.com.pragma.model.loandecisionresult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInstallment {
    private int number;
    private BigDecimal capitalPayment;
    private BigDecimal interestPayment;
    private BigDecimal remainingBalance;
}
