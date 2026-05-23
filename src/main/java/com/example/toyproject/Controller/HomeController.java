package com.example.toyproject.Controller;

import com.example.toyproject.DTO.response.ApiResponse;
import com.example.toyproject.DTO.response.ImageAnalysisDto;
import com.example.toyproject.Service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//반환되는 모든 데이터를 화면(HTML)이 아닌 JSON형식의 순수 데이터로 자동 변환하도록 설정
@RestController
//공통 기본 URL 경로 설정
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    // POST/api/home
    //consumes=... : API가 어떤 형태의 데이터를 받을지 제한
    @PostMapping(value = "/home", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadImage(
            //image라는 이름으로 첨부한 파일 데이터를 자바 객체로 변환해서 받음
            @RequestPart("image") MultipartFile image,
            //HTTP 요청의 헤더 영역에 담긴 값을 꺼내서 guestId로
            @RequestHeader("X-Guest-Id") String guestId) {

        //이미지 없이 요청한 경우
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("MISSING_IMAGE", "이미지를 첨부해주세요"));
        }
        //검증을 무사히 통과한 이미지 파일과 사용자 ID를 Servic계층으로 넘겨 처리 지시
        ImageAnalysisDto result = homeService.analyzeImage(image, guestId);
        //최종 결과 반환
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
