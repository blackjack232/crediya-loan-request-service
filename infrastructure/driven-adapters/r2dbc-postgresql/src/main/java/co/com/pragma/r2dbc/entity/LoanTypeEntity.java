package co.com.pragma.r2dbc.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "loan_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loan_type")
    private Long idLoanType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "minimum_amount", nullable = false)
    private Double minAmount;

    @Column(name = "maximum_amount", nullable = false)
    private Double maxAmount;

    @Column(name = "interest_rate", nullable = false)
    private Double interestRate;

    @Column(name = "automatic_validation", nullable = false)
    private Boolean automaticValidation;
}