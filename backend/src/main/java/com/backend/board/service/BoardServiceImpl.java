package com.backend.board.service;

import com.backend.board.domain.Board;
import com.backend.board.domain.BoardImage;
import com.backend.board.dto.BoardDTO;
import com.backend.board.dto.BoardImageDTO;
import com.backend.board.repository.BoardImageRepository;
import com.backend.board.repository.BoardRepository;
import com.backend.global.util.CustomFileUtil;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;

    private final BoardImageRepository boardImageRepository;

    private final MemberRepository memberRepository;

    private final ModelMapper modelMapper;

    private final CustomFileUtil customFileUtil;

    @Override
    public Page<BoardDTO> getBoardList(Pageable pageable) {

        log.info("BoardServiceImpl_getBoardList_gogo.....");

        Pageable result = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "boardNumber"));

        Page<Board> boardList = boardRepository.findWithAllMember(result);

        // 여기 익숙해져야함
        Page<BoardDTO> boardDTOList = boardList.map(board -> {

            List<BoardImageDTO> imageList = board.getImageList().stream()
                    .map(boardImage -> BoardImageDTO.builder()
                            .boardImageNumber(boardImage.getBoardImageNumber())
                            .imageUrl(boardImage.getImageUrl())
                            .build())
                    .toList();

            return BoardDTO.builder()
                    .boardNumber(board.getBoardNumber())
                    .memberEmail(board.getMemberEmail().getEmail())
                    .title(board.getTitle())
                    .contents(board.getContents())
                    .imageList(imageList)
                    .build();
        });

        return boardDTOList;

    }

    @Override
    public BoardDTO getBoard(Long boardNumber) {

        log.info("BoardServiceImpl_getBoard_gogo.....");

        Board board = boardRepository.findWithMember(boardNumber).orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));

//        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        List<BoardImageDTO> boardDTOList = board.getImageList().stream().map(boardImage -> BoardImageDTO.builder()
                .boardImageNumber(boardImage.getBoardImageNumber()).imageUrl(boardImage.getImageUrl()).build()).toList();

        BoardDTO boardDTO = BoardDTO.builder()
                .boardNumber(board.getBoardNumber())
                .memberEmail(board.getMemberEmail().getEmail())
                .title(board.getTitle())
                .contents(board.getContents())
                .imageList(boardDTOList)
                .build();

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

        if(boardDTO.getFiles() != null && !boardDTO.getFiles().isEmpty()) {
            List<String> fileNames = customFileUtil.saveFiles(boardDTO.getFiles());

            for(String fileName : fileNames) {
                BoardImage boardImage = BoardImage.builder()
                        .imageUrl(fileName)
                        .boardNumber(result)
                        .build();

                boardImageRepository.save(boardImage);

            }

        }

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
