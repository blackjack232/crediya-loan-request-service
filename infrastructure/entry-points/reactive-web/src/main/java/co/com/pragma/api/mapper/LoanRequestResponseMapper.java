package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.LoanRequestResponse;
import co.com.pragma.api.dto.response.LoanResponse;
import co.com.pragma.model.requests.Requests;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanRequestResponseMapper {
    LoanRequestResponse toResponse(Requests requests);
}