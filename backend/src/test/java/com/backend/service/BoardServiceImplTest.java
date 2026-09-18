package com.backend.service;

import com.backend.board.domain.Board;
import com.backend.board.dto.BoardDTO;
import com.backend.board.service.BoardService;
import com.backend.member.domain.Member;
import com.backend.member.domain.MemberRole;
import com.backend.member.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@SpringBootTest
@Log4j2
public class BoardServiceImplTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void insert() {

        Member member = Member.builder()
                .email("hjc135@naver.com")
                .pw("1111")
                .nickname("화투캡터용녀")
                .build();

        member.addRole(MemberRole.ADMIN);

        memberRepository.save(member);

        log.info(member);

        for(int i = 0; i < 30; i++) {

            BoardDTO boardDTO = BoardDTO.builder()
                    .memberEmail(member.getEmail())
                    .title("테스트용제목" + i)
                    .contents("테스트용내용" + i)
                    .build();

            boardService.insert(boardDTO, member.getEmail());

            log.info(boardDTO);

        }

    }

    @Test
    public void getBoardList() {

        int page = 1;

        int size = 10;

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "boradNumber"));

        Page<BoardDTO> boardDTOList = boardService.getBoardList(pageable);

        log.info(boardDTOList);

    }

    @Test
    public void getBoard() {

        // LazyInitializationException가 일어남 -> Board.memberEmail은 fetch = FetchType.LAZY라서
        // Repository에 @EntityGraph를 써서 memberEmail을 즉시 로딩하는 @Query(JPQL)을 새로 만들어서 해결
        Long boardNumber = 2L;

        BoardDTO boardDTO = boardService.getBoard(boardNumber);

        log.info(boardDTO);

    }

    @Test
    public void modify() {

        Member member = memberRepository.findById("hjc135@naver.com").orElseThrow();

        BoardDTO boardDTO = BoardDTO.builder()
                .boardNumber(3L)
                .memberEmail(member.getEmail())
                .title("수정용제목")
                .contents("수정용내용")
                .build();

        boardService.modify(boardDTO, member.getEmail());

    }

    @Test
    public void delete() {

        Long boardNumber = 5L;

        Member member = memberRepository.findById("hjc135@naver.com").orElseThrow();

        boardService.delete(boardNumber, member.getEmail());

    }

}
