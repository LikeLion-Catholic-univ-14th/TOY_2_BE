package com.example.toyproject.Service;

import com.example.toyproject.DTO.request.SpendingCreateDto;
import com.example.toyproject.DTO.request.SpendingUpdateDto;
import com.example.toyproject.DTO.response.SpendingDto;
import com.example.toyproject.DTO.response.TodaySpendingDto;
import com.example.toyproject.Entity.Spending;
import com.example.toyproject.Exception.CustomException;
import com.example.toyproject.Repository.SpendingRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
//DI(의존성 주입)처리를 할 때, 중복 코드를 줄이기 위해 사용
//초기화되지 않은 final 필드를 매개변수로 받는 생성자를 자동으로 생성해주는 역할
@RequiredArgsConstructor
//'읽기 전용(조회)'임을 알려줌
@Transactional(readOnly = true)
public class SpendingService {

    private final SpendingRepository spendingRepository;

    // 지출 목록 조회
    public List<SpendingDto> getSpendings(String guestId, Integer year, Integer month) {
        // year, month 없으면 현재 달 기준
        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();
        //YearMonth.now()은 null 방지를 위해 현재 달(now)을 기준으로 잡음
        //start = 그 달의 1일
        LocalDate start = ym.atDay(1);
        //end = 마지막 날
        LocalDate end = ym.atEndOfMonth();

        return spendingRepository
                .findByGuestIdAndPurchaseDateBetween(guestId, start, end) //DB에서 조건에 맞는 데이터 조회해서 가져오기
                .stream() //리스트 형식의 데이터를 가공하기 쉬운 상태로 만듦
                .map(SpendingDto::from) //데이터 타입 변환(Spending -> SPendingDto)
                .collect(Collectors.toList()); //변환이 끝난 데이터를 List로 묶어줌
    }

    // 지출 항목 등록
    @Transactional
    public SpendingDto createSpending(String guestId, SpendingCreateDto dto) {
        //입력받은 DTO의 데이터를 꺼내서, DB에 저장할 수 있는 원본 객체 형태의 Entity로 생성
        Spending spending = Spending.builder()
                .guestId(guestId) //사용자 아이디
                .imageUrl(dto.getImageUrl()) //이미지
                .itemName(dto.getItemName()) //상품명
                .category(dto.getCategory()) //카테고리
                .amount(dto.getAmount()) //가격
                .purchaseDate(dto.getPurchaseDate()) //구매 날짜
                .emotionTag(dto.getEmotionTag()) //감정 Tag
                .satisfactionLevel(dto.getSatisfactionLevel()) //만족도
                .memo(dto.getMemo()) //일기 메모
                .aiConfidence(dto.getAiConfidence()) //AI 만족 결과
                .build();
        //Entity 객체를 실제 DB에 저장
        return SpendingDto.from(spendingRepository.save(spending));
    }

    // 오늘 지출 조회 - 오늘 하루 동안 쓴 돈, 건수, 리스트를 싹 모아서 보여줌
    public TodaySpendingDto getTodaySpendings(String guestId) {
        //DB에서 '오늘' 발생한 특정 사용자의 Entity를 모두 조회
        List<Spending> spendings = spendingRepository
                .findByGuestIdAndPurchaseDate(guestId, LocalDate.now());
        //조회해온 Entity 리스트를 화면 응답용 DTO 리스트로 변환
        List<SpendingDto> dtos = spendings.stream()
                .map(SpendingDto::from)
                .collect(Collectors.toList());
        //오늘 지출한 금액만 추출 및 총합 계산
        int totalAmount = spendings.stream()
                .mapToInt(Spending::getAmount)
                .sum();
        //개별 데이터들을 하나의 종합 객체로 최종 변환
        return TodaySpendingDto.builder()
                .spendings(dtos)
                .totalAmount(totalAmount)
                .count(dtos.size())
                .build();
    }

    // 지출 항목 수정
    @Transactional
    public SpendingDto updateSpending(String guestId, Long id, SpendingUpdateDto dto) {
        //수정할 대상인 Entity를 DB에서 조회하고, 데이터가 없을 시, 예외 처리
        Spending spending = spendingRepository.findById(id)
                .orElseThrow(() -> new CustomException("NOT_FOUND", "해당 소비 기록이 없습니다"));

        //본인 기록인지 확인
        //데이터 수정 요청에 대한 권한 검증
        if (!spending.getGuestId().equals(guestId)) {
            //만약 일치하지 않을 경우 접근 차단을 위한 예외 처리
            throw new CustomException("FORBIDDEN", "수정 권한이 없습니다");
        }
        //Entity 내부의 필드 값들을 새로 전달받은 값들(dto)로 교체
        spending.update(
                dto.getItemName(), dto.getCategory(), dto.getAmount(),
                dto.getPurchaseDate(), dto.getEmotionTag(),
                dto.getSatisfactionLevel(), dto.getMemo()
        );
        //변경이 완료된 Entity를 응답용 DTO로 변환하여 반환
        return SpendingDto.from(spending);
    }

    // 지출 항목 삭제
    @Transactional
    public void deleteSpending(String guestId, Long id) {
        //삭제할 대상인 Entity를 DB에서 조회
        //데이터가 이미 삭제 or 없을 시, 예외 처리
        Spending spending = spendingRepository.findById(id)
                .orElseThrow(() -> new CustomException("NOT_FOUND", "해당 소비 기록이 없습니다"));
        //데이터 삭제 요청에 대한 권한 검증
        if (!spending.getGuestId().equals(guestId)) {
            //권한이 없을 경우 예외 처리
            throw new CustomException("FORBIDDEN", "삭제 권한이 없습니다");
        }
        //조회를 통해 찾은 Entity를 실제 DB에서 삭제
        spendingRepository.delete(spending);
    }
}
