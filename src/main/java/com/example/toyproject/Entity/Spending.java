package com.example.toyproject.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spendings") //실제 DB에 생성될 테이블 이름: spendings
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Spending {

    @Id //고유 식별자임을 선언
    //DB가 알아서 순서대로 자동 부여하도록 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //nullable=false: null값을 허용하지 않겠다는 제약
    @Column(nullable = false)
    private String guestId; //사용자 이름

    @Column(nullable = false)
    private String imageUrl; //상품 이미지

    @Column(nullable = false, length = 50)
    private String itemName; //상품명

    @Column(nullable = false)
    private String category; //카테고리

    @Column(nullable = false)
    private Integer amount; //가격

    @Column(nullable = false)
    private LocalDate purchaseDate; //구매 날짜

    @Column(nullable = false)
    private String emotionTag; //감정 태크

    @Column(nullable = false)
    private String satisfactionLevel; //만족도

    @Column(length = 200)
    private String memo; //일기 메모

    private Integer aiConfidence; //

    @Column(updatable = false)
    private LocalDateTime createdAt; //작성 날짜

    private LocalDateTime updatedAt; //생성 날짜
    //JPA가 데이터를 DB에 최초로 저장하기 직전에 메서드를 자동으로 실행시켜줌
    //현재 시간을 생성일과 수정일에 동시에 기록
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //Setter 대신 수정용 메서드를 따로 만듦 -> Entity 클래스에는 무분별하게 데이터를 바꿀 수 있는 @Setter을 사용 X
    //null이 들어오면 기존 값 유지 (PATCH 방식)
    //수정을 요청할 때, 모든 데이터를 다 보내지 않고, 바꿀 데이터만 보낼 수 있도록 함
    public void update(String itemName, String category, Integer amount,
                       LocalDate purchaseDate, String emotionTag,
                       String satisfactionLevel, String memo) {
        if (itemName != null) this.itemName = itemName;
        if (category != null) this.category = category;
        if (amount != null) this.amount = amount;
        if (purchaseDate != null) this.purchaseDate = purchaseDate;
        if (emotionTag != null) this.emotionTag = emotionTag;
        if (satisfactionLevel != null) this.satisfactionLevel = satisfactionLevel;
        if (memo != null) this.memo = memo;
    }
}
