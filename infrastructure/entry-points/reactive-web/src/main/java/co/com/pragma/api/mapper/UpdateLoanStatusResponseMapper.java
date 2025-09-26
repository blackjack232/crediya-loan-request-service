package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.UpdateLoanStatusResponse;
import co.com.pragma.model.requests.Requests;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpdateLoanStatusResponseMapper {

    UpdateLoanStatusResponse toResponse(Requests requests);

}
