package com.example.toyproject.DTO.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyzeResultDto {
    private String topEmotion;        //가장 많이 나온 감정
    private String topCategory;       //가장 많이 쓴 카테고리
    private Integer totalAmount;      //총 소비액
    private Integer totalCount;       //총 소비 건수
    private String aiInsight;         //AI가 생성한 텍스트 인사이트
    private String nextPrinciple;     //AI가 생성한 다음 소비 원칙
}
