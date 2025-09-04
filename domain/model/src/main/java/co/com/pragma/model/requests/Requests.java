package co.com.pragma.model.requests;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Requests {
    private Long idRequest;
    private Double amount;
    private Integer term;
    private String email;
    private Long idState;
    private Long idLoanType;
}
