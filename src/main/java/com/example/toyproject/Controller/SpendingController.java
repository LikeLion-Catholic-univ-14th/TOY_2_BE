package com.example.toyproject.Controller;

import com.example.toyproject.DTO.request.SpendingCreateDto;
import com.example.toyproject.DTO.request.SpendingUpdateDto;
import com.example.toyproject.DTO.response.ApiResponse;
import com.example.toyproject.DTO.response.SpendingDto;
import com.example.toyproject.DTO.response.TodaySpendingDto;
import com.example.toyproject.Service.SpendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spendings")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingService spendingService;

    //GET /api/spendings - 지출 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSpendings(
            @RequestHeader("X-Guest-Id") String guestId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        List<SpendingDto> spendings = spendingService.getSpendings(guestId, year, month);
        return ResponseEntity.ok(ApiResponse.ok(spendings));
    }

    //POST /api/spendings - 지출 항목 등록
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSpending(
            @RequestHeader("X-Guest-Id") String guestId,
            @RequestBody @Valid SpendingCreateDto dto) {

        SpendingDto saved = spendingService.createSpending(guestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    //GET /api/spendings/todays - 오늘 지출 조회
    //{id} 보다 위에 있어야 "todays"를 id로 인식하지 않음
    @GetMapping("/todays")
    public ResponseEntity<ApiResponse<?>> getTodaySpendings(
            @RequestHeader("X-Guest-Id") String guestId) {

        TodaySpendingDto result = spendingService.getTodaySpendings(guestId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    //PATCH /api/spendings/{id} - 지출 항목 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateSpending(
            @RequestHeader("X-Guest-Id") String guestId,
            @PathVariable Long id,
            @RequestBody SpendingUpdateDto dto) {

        SpendingDto updated = spendingService.updateSpending(guestId, id, dto);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    // DELETE /api/spendings/{id} - 지출 항목 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteSpending(
            @RequestHeader("X-Guest-Id") String guestId,
            @PathVariable Long id) {

        spendingService.deleteSpending(guestId, id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true)));
    }
}
