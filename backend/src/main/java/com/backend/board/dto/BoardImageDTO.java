package com.backend.board.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BoardImageDTO {

    private Long boardImageNumber;

    private String imageUrl;

}
