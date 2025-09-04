package co.com.pragma.api.dto.response;

import lombok.Data;
@Data
public class LoanResponse {
    private String name;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRate;
    private Boolean automaticValidation;

    }