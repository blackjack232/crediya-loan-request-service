package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.CapacityTranslatorLoanRequest;
import co.com.pragma.api.dto.request.LoanRequest;
import co.com.pragma.model.requests.Requests;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapacityTranslatorLoanRequestMapper {
    Requests toDomain(CapacityTranslatorLoanRequest request);
}
