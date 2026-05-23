package com.example.toyproject.Controller;

import com.example.toyproject.DTO.response.ApiResponse;
import com.example.toyproject.DTO.response.ReportDto;
import com.example.toyproject.Service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//반환되는 모든 데이터를 화면(HTML)이 아닌 JSON형식의 순수 데이터로 자동 변환하도록 설정
@RestController
//공통 기본 URL 경로 설정
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    // POST/api/report
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<?>> getReport(
            //HTTP 요청의 헤더 영역에 담긴 값을 꺼내서 guestId로
            @RequestHeader("X-Guest-Id") String guestId,
            //파라미터가 없으면 (year,month)에 null
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        //추출한 데이터를 Service 계층에 넘겨 통계 보고서 생성
        ReportDto report = reportService.getMonthlyReport(guestId, year, month);
        //JSON 형식으로 반환
        return ResponseEntity.ok(ApiResponse.ok(report));
    }
}
