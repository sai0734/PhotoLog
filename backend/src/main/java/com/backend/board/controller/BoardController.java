package com.backend.board.controller;

import com.backend.board.dto.BoardDTO;
import com.backend.board.service.BoardService;
import com.backend.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Log4j2
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('USER')")
    public Page<BoardDTO> getBoardList(PageRequestDTO pageRequestDTO) {

        log.info("BoardController_getBoardList_gogo.....");

        Page<BoardDTO> boardDTOList = boardService.getBoardList(pageRequestDTO.getPageable(Sort.unsorted()));

        return boardDTOList;

    }

    //  URL경로의 모양에 따라 사용하는 어노테이션이 다르다.
    // /api/board/1 -> Path Variable(경로변수) -> 이때는 @PathVariable 사용
    // /api/board?page=1&size=10 ->  Query String(쿼리 문자열) -> 이때는 @RequestParam 사용
    @GetMapping("/{boardNumber}")
    @PreAuthorize("hasAnyRole('USER')")
    public BoardDTO getBoard(@PathVariable Long boardNumber) {

        log.info("BoardController_getBoard_gogo.....");

        BoardDTO boardDTO = boardService.getBoard(boardNumber);

        return boardDTO;

    }

    // URL에 값이 담겨오지 않고 HTTP의 body에 JSON형태로 담겨오기 때문에 @RequestBody 사용
    // boardNumber를 반환하는 이유는 프론트에서 등록 후 상세페이지로 바로 이동하려고 할 때 필요하니까
    @PostMapping("/")
    @PreAuthorize("hasAnyRole('USER')")
    public Map<String, Long> register(@RequestBody BoardDTO boardDTO, Principal principal) {

        log.info("BoardController_register_gogo.....");

        String memberEmail = principal.getName();

        Long boardNumber = boardService.insert(boardDTO, memberEmail);

        return Map.of("등록번호", boardNumber);

    }

    @PutMapping("/{boardNumber}")
    @PreAuthorize("hasAnyRole('USER')")
    public Map<String, String> modify(@PathVariable Long boardNumber, @RequestBody BoardDTO boardDTO, Principal principal) {

        log.info("BoardController_modify_gogo.....");

        boardDTO.setBoardNumber(boardNumber);

        String memberEmail = principal.getName();

        boardService.modify(boardDTO, memberEmail);

        return Map.of("result", "modified");

    }

    @DeleteMapping("/{boardNumber}")
    @PreAuthorize("hasAnyRole('USER')")
    public Map<String, String> delete(@PathVariable Long boardNumber, Principal principal) {

        log.info("BoardController_delete_gogo.....");

        String memberEmail = principal.getName();

        boardService.delete(boardNumber, memberEmail);

        return Map.of("result", "deleted");

    }

}
