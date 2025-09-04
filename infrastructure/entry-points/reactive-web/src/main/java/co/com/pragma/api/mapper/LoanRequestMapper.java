package co.com.pragma.api.mapper;
import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.model.loantype.LoanType;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface LoanRequestMapper {
    LoanType toDomain(LoanRequest request);
}