package com.example.toyproject.DTO.response;

import com.example.toyproject.Entity.Spending;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SpendingDto {

    private Long id;
    private String guestId;
    private String imageUrl;
    private String itemName;
    private String category;
    private Integer amount;
    private LocalDate purchaseDate;
    private String emotionTag;
    private String satisfactionLevel;
    private String memo;
    private Integer aiConfidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public static SpendingDto from(Spending spending) {
        return SpendingDto.builder()
                .id(spending.getId())
                .guestId(spending.getGuestId())
                .imageUrl(spending.getImageUrl())
                .itemName(spending.getItemName())
                .category(spending.getCategory())
                .amount(spending.getAmount())
                .purchaseDate(spending.getPurchaseDate())
                .emotionTag(spending.getEmotionTag())
                .satisfactionLevel(spending.getSatisfactionLevel())
                .memo(spending.getMemo())
                .aiConfidence(spending.getAiConfidence())
                .createdAt(spending.getCreatedAt())
                .updatedAt(spending.getUpdatedAt())
                .build();
    }
}
