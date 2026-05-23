package com.example.toyproject.DTO.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ReportDto {
    private Integer totalAmount;
    private Integer totalCount;
    private Map<String, CategoryStatDto> byEmotion;
    private Map<String, CategoryStatDto> byCategory;
    private Map<String, Integer> satisfactionRatio; // 잘샀다:60, 왜샀지:20 ...
    private String topEmotion;
    private String aiInsight;
    private String nextPrinciple;

    @Getter
    @Builder
    public static class CategoryStatDto {
        private Integer count;
        private Integer amount;
    }
}