package co.com.pragma.api.dto.request;

import lombok.Data;

@Data
public class UpdateLoanStatusRequest {

        private Long idLoanRequest;
        private Long idState;
        private String identification;

}