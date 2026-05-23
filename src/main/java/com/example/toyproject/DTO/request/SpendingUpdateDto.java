package com.example.toyproject.DTO.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// PATCH라서 모든 필드 선택
@Getter
@NoArgsConstructor
public class SpendingUpdateDto {

    @Size(max = 50)
    private String itemName;

    private String category;
    private Integer amount;
    private LocalDate purchaseDate;
    private String emotionTag;
    private String satisfactionLevel;

    @Size(max = 200)
    private String memo;
}
