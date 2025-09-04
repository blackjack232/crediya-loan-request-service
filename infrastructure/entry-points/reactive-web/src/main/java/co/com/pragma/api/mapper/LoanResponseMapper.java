package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.LoanResponse;
import co.com.pragma.model.loantype.LoanType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanResponseMapper {
    LoanResponse toResponse(LoanType loanType);
}