package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.AnalyzeResultDto;
import com.example.toyproject.Entity.Spending;
import com.example.toyproject.Exception.CustomException;
import com.example.toyproject.Repository.SpendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeService {

    private final SpendingRepository spendingRepository;

    public AnalyzeResultDto analyze(String guestId, Integer year, Integer month) {

        // 1. 해당 월 소비 데이터 가져오기
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        List<Spending> spendings = spendingRepository
                .findByGuestIdAndPurchaseDateBetween(
                        guestId, ym.atDay(1), ym.atEndOfMonth());

        // 2. 데이터 없으면 예외 처리
        if (spendings.isEmpty()) {
            throw new CustomException("NOT_FOUND", "분석할 소비 데이터가 없습니다");
        }

        // 3. 총 소비액, 건수
        int totalAmount = spendings.stream()
                .mapToInt(Spending::getAmount).sum();

        // 4. 가장 많이 나온 감정
        String topEmotion = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getEmotionTag, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        // 5. 가장 많이 쓴 카테고리
        String topCategory = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        // 6. AI 인사이트 (OpenAI 키 받으면 여기를 교체하면 돼요)
        String aiInsight = topEmotion + " 소비가 가장 많았어요. "
                + topCategory + " 카테고리에 가장 많이 지출했어요.";
        String nextPrinciple = "충동구매 전 하루 뒤 다시 확인해보세요.";

        return AnalyzeResultDto.builder()
                .topEmotion(topEmotion)
                .topCategory(topCategory)
                .totalAmount(totalAmount)
                .totalCount(spendings.size())
                .aiInsight(aiInsight)
                .nextPrinciple(nextPrinciple)
                .build();
    }
}
