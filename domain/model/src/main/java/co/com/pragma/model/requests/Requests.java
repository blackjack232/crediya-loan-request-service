package co.com.pragma.model.requests;
import lombok.*;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Requests {

    private Long idRequest;
    private Double amount;
    private Integer term;
    private String email;
    private String identification;
    private Long idState;
    private Long idLoanType;
}
