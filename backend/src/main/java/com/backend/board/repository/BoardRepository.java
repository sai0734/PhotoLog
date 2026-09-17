package com.backend.board.repository;

import com.backend.board.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 검색기능
    @Query("select b from Board b where b.title like concat('%', :keyword, '%')")
    Page<Board> findKeyword (@Param("keyword") String keyword, Pageable pageable);

    // LazyInitializationException때문에 새롭게 만듬
    // @EntityGraph(attributePaths = "memberEmail") -> Fetch.LAZY를 join을 이용해 한번에 불러옴
    @EntityGraph(attributePaths = "memberEmail")
    @Query("select b from Board b where b.boardNumber = :boardNumber")
    Optional<Board> findWithMember(@Param("boardNumber") Long boardNumber);

    @EntityGraph(attributePaths = "memberEmail")
    @Query("select b from Board b")
    Page<Board> findWithAllMember(Pageable pageable);

}
