package co.com.pragma.api.dto.request;

import lombok.Data;

@Data
public class UpdateLoanStatusRequest {

        private Long idRequest;;
        private Long idState;
        private String identification;

}