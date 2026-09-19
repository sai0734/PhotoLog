package com.backend.board.service;

import com.backend.board.dto.BoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {

    // 전체조회
    Page<BoardDTO> getBoardList(Pageable pageable);

    // 단건조회
    BoardDTO getBoard(Long boardNumber);

    // 등록
    Long insert(BoardDTO boardDTO, String memberEmail);

    // 수정
    void modify(BoardDTO boardDTO, String memberEmail);

    // 삭제
    void delete(Long boardNumber, String memberEmail);

}
