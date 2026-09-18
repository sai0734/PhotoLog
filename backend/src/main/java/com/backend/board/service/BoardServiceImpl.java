package com.backend.board.service;

import com.backend.board.domain.Board;
import com.backend.board.dto.BoardDTO;
import com.backend.board.repository.BoardRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;

    private final MemberRepository memberRepository;

    private final ModelMapper modelMapper;

    @Override
    public Page<BoardDTO> getBoardList(Pageable pageable) {

        log.info("BoardServiceImpl_getBoardList_gogo.....");

        Pageable result = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "boardNumber"));

        Page<Board> boardList = boardRepository.findWithAllMember(result);

        Page<BoardDTO> boardDTOList = boardList.map(board -> modelMapper.map(board, BoardDTO.class));

        return boardDTOList;

    }

    @Override
    public BoardDTO getBoard(Long boardNumber) {

        log.info("BoardServiceImpl_getBoard_gogo.....");

        Board board = boardRepository.findWithMember(boardNumber).orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));

        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        return boardDTO;
    }

    @Override
    public Long insert(BoardDTO boardDTO, String memberEmail) {

        log.info("BoardServiceImpl_insert_gogo.....");

//        Board board = modelMapper.map(boardDTO, Board.class);

        Member member = memberRepository.findById(memberEmail).orElseThrow();

        Board board = Board.builder()
                .memberEmail(member)
                .title(boardDTO.getTitle())
                .contents(boardDTO.getContents())
                .build();

        Board result = boardRepository.save(board);

        return result.getBoardNumber();

    }

    @Override
    public void modify(BoardDTO boardDTO, String memberEmail) {

        log.info("BoardServiceImpl_modify_gogo.....");

        Board board = boardRepository.findWithMember(boardDTO.getBoardNumber()).orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));

        if(board.getMemberEmail().getEmail().equals(memberEmail)) {

            board.change(boardDTO.getTitle(), boardDTO.getContents());

            boardRepository.save(board);

        } else {
            throw new AccessDeniedException("나의 글이 아닙니다.");
        }

    }

    @Override
    public void delete(Long boardNumber, String memberEmail) {

        log.info("BoardServiceImpl_delete_gogo.....");

        Board board = boardRepository.findWithMember(boardNumber).orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));

        if(board.getMemberEmail().getEmail().equals(memberEmail)) {

            boardRepository.delete(board);

        } else {
            throw new AccessDeniedException("나의 글이 아닙니다.");
        }

    }
}
