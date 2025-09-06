package co.com.pragma.api.dto.response;

import lombok.Data;
@Data
public class LoanResponse {
    private Double amount;
    private Integer term;
    private String email;

    }