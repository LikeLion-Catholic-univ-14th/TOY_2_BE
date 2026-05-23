package com.example.toyproject.DTO.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodaySpendingDto {
    private List<SpendingDto> spendings;
    private Integer totalAmount;
    private Integer count;
}
