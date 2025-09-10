package co.com.pragma.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {
    private Long identification;
    private String email;
    private BigDecimal amount;
    private int term;
    private Long id_loan_type;
}