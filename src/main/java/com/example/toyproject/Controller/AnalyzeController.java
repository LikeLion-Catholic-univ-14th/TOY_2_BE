package com.example.toyproject.Controller;

import com.example.toyproject.DTO.request.AnalyzeRequestDto;
import com.example.toyproject.DTO.response.AnalyzeResultDto;
import com.example.toyproject.DTO.response.ApiResponse;
import com.example.toyproject.Service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//반환되는 모든 데이터를 화면(HTML)이 아닌 JSON형식의 순수 데이터로 자동 변환하도록 설정
@RestController
//공통 기본 URL 경로 설정
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyzeController {
    //실제 분석 로직을 수행할 객체 주입받아 연결
    private final AnalyzeService analyzeService;

    //저장된 데이터 기반 카테고리 통계 및 인사이트 반환
    // POST/api/analyze
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<?>> analyze(
            //HTTP 요청의 헤더 영역에 담긴 값을 꺼내서 guestId로
            @RequestHeader("X-Guest-Id") String guestId,
            //요청 본문(Body)에 담긴 JSON 데이터를 자바 객체로 자동 변환하여 받아옴
            @RequestBody AnalyzeRequestDto dto) {
        //사용자 ID와 분석 요청 데이터(dto)를 Service 계층에 넘겨서 실제 분석 작업을 지시
        AnalyzeResultDto result = analyzeService.analyze(guestId, dto.getYear(), dto.getMonth());
        //HTTP 응답 상태 코드를 정상 처리를 의미하는 200으로 설정
        //ApiResponse.ok(result): 최종 결과(result)를 프로젝트의 공통 응답 구격(ApiResponse)으로 변환
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
