package co.com.pragma.api.dto.request;

import lombok.Data;

@Data
public class LoanRequest {
    private String name;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}