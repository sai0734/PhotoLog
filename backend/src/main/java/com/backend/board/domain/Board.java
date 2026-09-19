package com.backend.board.domain;

import com.backend.global.domain.BaseEntity;
import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"memberEmail", "imageList"})
@Table(name = "tbl_board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_number")
    private Long boardNumber;

    // 다대일관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", nullable = false)
    private Member memberEmail;

    // 일대다관계 -> cascade는 board삭제 시 해당 필드 같이 삭제, mappedBy는 외래키(FK)를 가지고 있는 녀석
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "boardNumber")
    private List<BoardImage> imageList;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "contents", length = 2000 , nullable = true)
    private String contents;

    public void change(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

}
