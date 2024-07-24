package com.ecom.rewards.repositories;

import com.ecom.rewards.models.CustomerRewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRewardsRepository extends JpaRepository<CustomerRewards, Long> {

    @Query(
            name = "CustomerRewards.findRewardPointsByMonthAndYear",
            value = "select cr from CustomerRewards cr where month(cr.invoiceDate)=:monthValue and year(cr.invoiceDate)=:yearValue"
    )
    List<CustomerRewards> findRewardPointsByMonthAndYear(@Param("monthValue") Integer month, @Param("yearValue") Integer year);

}
