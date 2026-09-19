package com.backend.board.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BoardDTO {

    private Long boardNumber;

    private String memberEmail;

    private String title;

    private String contents;

    private List<MultipartFile> files;

    private List<BoardImageDTO> imageList;

    private List<Long> keepImageNumbers;

}
