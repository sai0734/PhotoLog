package com.backend.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.member.domain.Member;

@Mapper
public interface MemberMapper {

  // 회원 정보 + 권한 목록을 함께 조회 (JPA의 getWithRoles()를 대체)
  Member selectByEmail(@Param("email") String email);

  void insert(Member member);

  void insertRole(@Param("email") String email, @Param("role") String role);

  void deleteRoles(@Param("email") String email);

  void update(Member member);

}
