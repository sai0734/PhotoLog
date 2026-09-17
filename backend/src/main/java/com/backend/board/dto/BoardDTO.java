package com.backend.board.dto;

import com.backend.member.domain.Member;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BoardDTO {

    private Long boardNumber;

    private Member memberEmail;

    private String title;

    private String contents;

    private List<String> imageUrl;

}
