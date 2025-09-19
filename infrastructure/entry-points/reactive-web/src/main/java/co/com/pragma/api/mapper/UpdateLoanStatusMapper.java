package co.com.pragma.api.mapper;


import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.api.dto.request.UpdateLoanStatusRequest;
import co.com.pragma.api.dto.response.LoanResponse;
import co.com.pragma.model.requests.Requests;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * Mapper encargado de convertir un {@link UpdateLoanStatusRequest}
 * en el modelo de dominio {@link Requests}.
 */
@Mapper(componentModel = "spring")
public interface UpdateLoanStatusMapper {
    Requests toDomain(UpdateLoanStatusRequest request);
}


