package com.backend.board.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "tbl_board_image")
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardImageNumber;

    private String imageUrl;

    // Board Entity에 mappedBy로 묶여있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_number")
    private Board boardNumber;

}
