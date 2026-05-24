package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.AnalyzeResultDto;
import com.example.toyproject.Entity.Spending;
import com.example.toyproject.Exception.CustomException;
import com.example.toyproject.Repository.SpendingRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)


public class AnalyzeService {

    private final SpendingRepository spendingRepository;

    @Value("${openai.api-key}")
    private String openAiApiKey;

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
        OpenAiService openAiService = new OpenAiService(openAiApiKey, Duration.ofSeconds(30));

        String prompt = String.format("""
        이번 달 소비 데이터입니다.
        - 총 소비액: %d원
        - 총 건수: %d건
        - 가장 많은 감정: %s
        - 가장 많은 카테고리: %s
        
        위 데이터를 바탕으로 아래 JSON 형식으로만 답해주세요.
        {
          "aiInsight": "한 줄 소비 인사이트",
          "nextPrinciple": "다음 소비 원칙 추천"
        }
        """, totalAmount, spendings.size(), topEmotion, topCategory);

        ChatMessage userMessage = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(userMessage))
                .maxTokens(200)
                .build();

        ChatCompletionResult aiResult = openAiService.createChatCompletion(request);
        String responseText = aiResult.getChoices().get(0).getMessage().getContent();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseText);

        String aiInsight = json.get("aiInsight").asText();
        String nextPrinciple = json.get("nextPrinciple").asText();

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
