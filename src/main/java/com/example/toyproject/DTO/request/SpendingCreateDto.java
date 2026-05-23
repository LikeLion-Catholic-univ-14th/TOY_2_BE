package com.example.toyproject.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// POST /api/spendings 요청 바디
@Getter
@NoArgsConstructor
public class SpendingCreateDto {

    @NotBlank(message = "이미지 URL은 필수입니다")
    private String imageUrl;

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 50, message = "상품명은 50자 이하입니다")
    private String itemName;

    @NotBlank(message = "카테고리는 필수입니다")
    private String category;

    @NotNull(message = "금액은 필수입니다")
    private Integer amount;

    @NotNull(message = "구매일은 필수입니다")
    private LocalDate purchaseDate;

    @NotBlank(message = "감정 태그는 필수입니다")
    private String emotionTag;

    @NotBlank(message = "만족도는 필수입니다")
    private String satisfactionLevel;

    @Size(max = 200, message = "메모는 200자 이하입니다")
    private String memo;

    private Integer aiConfidence;
}
