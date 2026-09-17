package com.backend.repository;

import com.backend.board.domain.Board;
import com.backend.board.repository.BoardRepository;
import com.backend.member.domain.Member;
import com.backend.member.domain.MemberRole;
import com.backend.member.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.util.List;

@SpringBootTest
@Log4j2
public class BoardRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

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

        for (int i = 0; i < 5; i++) {

            Board board = Board.builder()
                    .memberEmail(member)
                    .title("테스트용 제목" + i)
                    .contents("테스트용 내용" + i)
                    .build();

            log.info(board);

            boardRepository.save(board);

        }

    }

    @Test
    public void getBoard() {

        Board board = boardRepository.findById(1L).orElseThrow();

        log.info(board);

    }

    @Test
    public void getBoardList() {

        List<Board> boardList = boardRepository.findAll();

        log.info(boardList);
    }

    @Test
    public void update() {

        Board board = boardRepository.findById(1L).orElseThrow();

        board.change("수정제목", "수정내용");

        boardRepository.save(board);

        log.info(board);

    }

    @Test
    public void delete() {

        Board board = boardRepository.findById(1L).orElseThrow();

        boardRepository.delete(board);

    }

}
