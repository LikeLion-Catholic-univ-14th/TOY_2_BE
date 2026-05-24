package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.ImageAnalysisDto;
import com.example.toyproject.Exception.CustomException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class HomeService {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    //analyzeImage() 안의 임시 응답 부분을 실제 OpenAI Vision 호출로 교체
    public ImageAnalysisDto analyzeImage(MultipartFile image, String guestId) {

        //이미지 저장(로컬 임시 저장)
        String imageUrl = saveImageLocally(image);
        //이미지를 Base64로 변환
        //Base64=화면에 깨지지 않는 안전한 텍스트 형식(ASCII문자)으로 변환하는 방식
        String base64Image;
        try {
            byte[] imageBytes = image.getBytes();
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "이미지 변환에 실패했습니다");
        }
        //OpenAI Vision API 호출
        OpenAiService openAiService = new OpenAiService(openAiApiKey, Duration.ofSeconds(30));

        String prompt = """
                이 이미지는 구매한 상품이나 영수증 사진입니다.
                아래 항목을 JSON 형식으로만 답해주세요. 다른 말은 하지 마세요.
                {
                  "itemName": "상품명",
                  "category": "카페/식비/패션/뷰티/생활/취미/선물/기타 중 하나",
                  "amount": 숫자만,
                  "recommendedEmotion": "잘 샀다/왜 샀지/기분전환/스트레스/충동구매/보상심리/필요해서/관계선물 중 하나",
                  "aiConfidence": 0에서 100 사이 숫자
                }
                """;
        ChatMessage userMessage = new ChatMessage("user", prompt
                + "\n\n[이미지 데이터(base64)]: " + base64Image);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(userMessage))
                .maxTokens(500)
                .build();
        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String responseText = result.getChoices().get(0).getMessage().getContent();

            //JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(responseText);

            return ImageAnalysisDto.builder()
                    .itemName(json.get("itemName").asText())
                    .category(json.get("category").asText())
                    .amount(json.get("amount").asInt())
                    .purchaseDate(LocalDate.now())
                    .recommendedEmotion(json.get("recommendedEmotion").asText())
                    .aiConfidence(json.get("aiConfidence").asInt())
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            //AI 분석 실패 시 기본값 반환
            throw new CustomException("ANALYZE_FAILED", "상품을 인식하지 못했어요");
        }
    }

    //이미지 로컬 저장 메서드
    private String saveImageLocally(MultipartFile image) {
        try {
            //저장 경로 설정
            String uploadDir = "uploads/";
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            //폴더 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            //파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + fileName;

        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "이미지 저장에 실패했습니다");
        }
    }
}
