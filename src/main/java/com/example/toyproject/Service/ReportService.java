package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.ReportDto;
import com.example.toyproject.Entity.Spending;
import com.example.toyproject.Repository.SpendingRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
//DI(의존성 주입)처리를 할 때, 중복 코드를 줄이기 위해 사용
//초기화되지 않은 final 필드를 매개변수로 받는 생성자를 자동으로 생성해주는 역할
@RequiredArgsConstructor
//'읽기 전용(조회)'임을 알려줌
@Transactional(readOnly = true)
public class ReportService {

    private final SpendingRepository spendingRepository;

    public ReportDto getMonthlyReport(String guestId, Integer year, Integer month) {
        //year, month 없으면 현재 달 기준
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        List<Spending> spendings = spendingRepository
                .findByGuestIdAndPurchaseDateBetween(
                        guestId, ym.atDay(1), ym.atEndOfMonth());
        //총 지출액 계산
        int totalAmount = spendings.stream().mapToInt(Spending::getAmount).sum();

        //감정별 통계
        //전체 지출 데이터를 '감정 태그' 기준으로 분류한 뒤, 각 감정별로 발생 건수, 누적 금액 계산
        //Map 구조(키-값)으로 생성 {"기쁨": (건수: 5,금액 합계: 50000)}
        Map<String, ReportDto.CategoryStatDto> byEmotion = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getEmotionTag,
                        Collectors.collectingAndThen(Collectors.toList(), list ->
                                ReportDto.CategoryStatDto.builder()
                                        .count(list.size())
                                        .amount(list.stream().mapToInt(Spending::getAmount).sum())
                                        .build()
                        )
                ));

        //카테고리별 통계
        //만족도(상,중,하)
        Map<String, ReportDto.CategoryStatDto> byCategory = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getCategory,
                        Collectors.collectingAndThen(Collectors.toList(), list ->
                                ReportDto.CategoryStatDto.builder()
                                        .count(list.size())
                                        .amount(list.stream().mapToInt(Spending::getAmount).sum())
                                        .build()
                        )
                ));

        //만족도 비율 계산
        //전체 지출 데이터를 '만족도' 기준으로 분류, 횟수 카운트
        //만
        Map<String, Integer> satisfactionRatio = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getSatisfactionLevel,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        //가장 많이 나온 감정
        String topEmotion = byEmotion.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().getCount())) //검수(count)를 기주느로 비교하여 가장 값이 큰 항목(최댓값) 찾음
                .map(Map.Entry::getKey) //최댓값을 가진 항목 전체에서 '감정 이름'만 추출
                .orElse("없음");
        //error가 나지 않도록 "없음"이라고 기본 문자열 반환

        //계산된 모든 개별 통계 데이터들을 하나의 큰 덩어리로 묶어서 변환
        return ReportDto.builder()
                .totalAmount(totalAmount)
                .totalCount(spendings.size())
                .byEmotion(byEmotion)
                .byCategory(byCategory)
                .satisfactionRatio(satisfactionRatio)
                .topEmotion(topEmotion)
                .aiInsight("이번 달 가장 많이 나타난 감정은 " + topEmotion + "이에요.")
                .nextPrinciple("충동구매 전 하루 뒤 다시 확인해보세요.")
                .build();
    }
}
