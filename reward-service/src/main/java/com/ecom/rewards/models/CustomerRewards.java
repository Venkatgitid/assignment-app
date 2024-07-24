package com.ecom.rewards.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "CUSTOMER_REWARDS")
public class CustomerRewards {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private long id;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "INVOICE_ID")
    private long invoiceId;

    @Column(name = "INVOICE_DATE")
    private LocalDate invoiceDate;

    @Column(name = "INVOICE_AMOUNT")
    private double invoiceAmount;

    @Column(name = "REWARDS_POINT")
    private double rewardsPoint;

}
