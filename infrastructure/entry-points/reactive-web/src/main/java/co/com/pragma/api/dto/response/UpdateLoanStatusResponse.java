package co.com.pragma.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el payload recibido en el endpoint de actualización de estado de préstamo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLoanStatusResponse {
    private Long idRequest;
    private java.math.BigDecimal amount;
    private Integer term;
    private String email;
    private String status;   // Nombre del estado (opcional, puede venir null)
    private Long idState;   // Nuevo id del estado
}
