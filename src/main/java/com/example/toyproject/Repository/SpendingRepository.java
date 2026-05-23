package com.example.toyproject.Repository;

import com.example.toyproject.Entity.Spending;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpendingRepository extends JpaRepository<Spending, Long> {

    // 월별 조회
    List<Spending> findByGuestIdAndPurchaseDateBetween(
            String guestId, LocalDate start, LocalDate end);

    // 오늘 조회
    List<Spending> findByGuestIdAndPurchaseDate(
            String guestId, LocalDate date);

    // guestId 전체 조회
    List<Spending> findByGuestId(String guestId);
}
