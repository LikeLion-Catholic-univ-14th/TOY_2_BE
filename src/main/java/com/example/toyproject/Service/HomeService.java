package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.ImageAnalysisDto;
import com.example.toyproject.Exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Service
public class HomeService {

    @Value("${gemini.api-key}")          //application.properties에서 Gemini AI API 키 가져오기
    private String geminiApiKey;

    public ImageAnalysisDto analyzeImage(MultipartFile image, String guestId) {

        //이미지 로컬 저장
        String imageUrl = saveImageLocally(image);

        //이미지를 Base64로 변환
        String base64Image;
        String mimeType = image.getContentType();
        try {
            byte[] imageBytes = image.getBytes();
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "이미지 변환에 실패했습니다");
        }

        //Gemini Vision API 호출
        WebClient webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .build();

        String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [
                      {
                        "text": "이 이미지는 구매한 상품이나 영수증입니다. 마크다운 기호 없이 순수 JSON으로만 답해주세요: { \\"itemName\\": \\"상품명\\", \\"category\\": \\"카페/식비/패션/뷰티/생활/취미/선물/기타 중 하나\\", \\"amount\\": 0, \\"recommendedEmotion\\": \\"잘 샀다/왜 샀지/기분전환/스트레스/충동구매/보상심리/필요해서/관계선물 중 하나\\", \\"aiConfidence\\": 0 }"
                      },
                      {
                        "inline_data": {
                          "mime_type": "%s",
                          "data": "%s"
                        }
                      }
                    ]
                  }]
                }
                """, mimeType, base64Image);

        try {
            String response = webClient.post()
                    //모델명: gemini-2.5-flash
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> new RuntimeException("Gemini API 오류: " + errorBody))
                    )
                    .bodyToMono(String.class)
                    .block();

            // 4. Gemini 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            String content = json.get("candidates").get(0)
                    .get("content").get("parts").get(0)
                    .get("text").asText();

            content = content.replace("```json", "").replace("```", "").trim();

            JsonNode resultJson = mapper.readTree(content);

            return ImageAnalysisDto.builder()
                    .itemName(resultJson.get("itemName").asText())
                    .category(resultJson.get("category").asText())
                    .amount(resultJson.get("amount").asInt())
                    .purchaseDate(LocalDate.now())
                    .recommendedEmotion(resultJson.get("recommendedEmotion").asText())
                    .aiConfidence(resultJson.get("aiConfidence").asInt())
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("ANALYZE_FAILED", "상품을 인식하지 못했어요: " + e.getMessage());
        }
    }

    //이미지 로컬 저장 메서드
    private String saveImageLocally(MultipartFile image) {
        try {
            String uploadDir = "uploads/";
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/" + uploadDir + fileName;
        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "이미지 저장에 실패했습니다");
        }
    }
}