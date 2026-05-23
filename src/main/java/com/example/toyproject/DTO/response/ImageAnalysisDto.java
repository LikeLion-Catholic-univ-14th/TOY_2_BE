package com.example.toyproject.DTO.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// POST /api/home 응답
@Getter
@Builder
public class ImageAnalysisDto {
    private String itemName;
    private String category;
    private Integer amount;
    private LocalDate purchaseDate;
    private String recommendedEmotion;
    private Integer aiConfidence;
    private String imageUrl;
}
