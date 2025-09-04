package co.com.pragma.model.state;
import lombok.*;
//import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class State {
    private Long idState;
    private String name;
    private String description;
}
