package co.com.pragma.model.loandecisionresult;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanDecisionResult {
    private String decision;            // APPROVED | REJECTED | MANUAL_REVIEW
    private BigDecimal monthlyInstallment;  // cuota del nuevo prestamo
    private BigDecimal availableCapacity;   // capacidad disponible
    private BigDecimal debtLoad;            // deuda mensual actual (opcional, útil para auditoría)
    private List<PaymentInstallment> paymentPlan;
}

