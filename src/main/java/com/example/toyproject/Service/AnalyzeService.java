package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.AnalyzeResultDto;
import com.example.toyproject.Entity.Spending;
import com.example.toyproject.Exception.CustomException;
import com.example.toyproject.Repository.SpendingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeService {

    private final SpendingRepository spendingRepository;

    @Value("${gemini.api-key}")          // ← application.properties에서 키 가져오기
    private String geminiApiKey;

    public AnalyzeResultDto analyze(String guestId, Integer year, Integer month) {

        // 1. 해당 월 소비 데이터 가져오기
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        List<Spending> spendings = spendingRepository
                .findByGuestIdAndPurchaseDateBetween(
                        guestId, ym.atDay(1), ym.atEndOfMonth());

        //데이터 없으면 예외 처리
        if (spendings.isEmpty()) {
            throw new CustomException("NOT_FOUND", "분석할 데이터가 없습니다");
        }

        //통계 계산
        int totalAmount = spendings.stream()
                .mapToInt(Spending::getAmount).sum();

        String topEmotion = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getEmotionTag, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        String topCategory = spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        //Gemini API 요청 바디 구성
        String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [{
                      "text": "총 소비액: %d원, 가장 많은 감정: %s, 카테고리: %s. 마크다운 없이 순수 JSON으로만 답해줘: {\\"aiInsight\\":\\"한 줄 인사이트\\", \\"nextPrinciple\\":\\"소비 원칙\\"}"
                    }]
                  }]
                }
                """, totalAmount, topEmotion, topCategory);

        try {
            //Gemini API 호출
            RestTemplate restTemplate = new RestTemplate();

            //모델명: gemini-2.5-flash
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey.trim();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);

            //응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            String content = json.get("candidates").get(0)
                    .get("content").get("parts").get(0)
                    .get("text").asText();

            //마크다운 코드블록 제거
            content = content.replace("```json", "").replace("```", "").trim();

            JsonNode resultJson = mapper.readTree(content);

            return AnalyzeResultDto.builder()
                    .topEmotion(topEmotion)
                    .topCategory(topCategory)
                    .totalAmount(totalAmount)
                    .totalCount(spendings.size())
                    .aiInsight(resultJson.get("aiInsight").asText())
                    .nextPrinciple(resultJson.get("nextPrinciple").asText())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("ANALYZE_FAILED", "분석 실패: " + e.getMessage());
        }
    }
}