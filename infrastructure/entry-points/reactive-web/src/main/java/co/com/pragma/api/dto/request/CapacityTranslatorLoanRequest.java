package co.com.pragma.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class CapacityTranslatorLoanRequest {
    private Long identification;
    private String email;
    private BigDecimal amount;
    private int term;
    private BigDecimal income;
    private Long idLoanType;

}
