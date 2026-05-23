package com.example.toyproject.Service;

import com.example.toyproject.DTO.response.ImageAnalysisDto;
import com.example.toyproject.Exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomeService {

    //OpenAI 키 받으면 application.properties에서 주입받을 거예요
    //@Value("${openai.api-key}")
    //private String openAiApiKey;

    //analyzeImage() 안의 임시 응답 부분을 실제 OpenAI Vision 호출로 교체

    public ImageAnalysisDto analyzeImage(MultipartFile image, String guestId) {

        //이미지 저장 (로컬 임시 저장)
        String imageUrl = saveImageLocally(image);

        //OpenAI Vision 분석
        //키 받으면 이 부분을 실제 OpenAI 호출 코드로 교체하면 돼요
        //지금은 임시 응답 반환
        return ImageAnalysisDto.builder()
                .itemName("상품명을 인식하지 못했어요")
                .category("기타")
                .amount(0)
                .purchaseDate(LocalDate.now())
                .recommendedEmotion("기분전환")
                .aiConfidence(0)
                .imageUrl(imageUrl)
                .build();
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
