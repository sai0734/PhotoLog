package com.backend.member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.member.dto.MemberModifyDTO;
import com.backend.member.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService {

  private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
  public void modifyMember(MemberModifyDTO memberModifyDTO) {

    Member member = Optional.ofNullable(memberMapper.selectByEmail(memberModifyDTO.getEmail()))
        .orElseThrow();

    member.changePw(passwordEncoder.encode(memberModifyDTO.getPw()));
    member.changeSocial(false);
    member.changeNickname(memberModifyDTO.getNickname());

    memberMapper.update(member);

  }
}
