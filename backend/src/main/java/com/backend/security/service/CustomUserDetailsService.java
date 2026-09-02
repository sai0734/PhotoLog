package com.backend.security.service;

import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.backend.member.domain.Member;
import com.backend.member.dto.MemberDTO;
import com.backend.member.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

  private final MemberMapper memberMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    log.info("----------------loadUserByUsername-----------------------------");

Member member = memberMapper.selectByEmail(username);

    if(member == null){
      throw new UsernameNotFoundException("Not Found");
    }

    MemberDTO memberDTO = new MemberDTO(
            member.getEmail(),
            member.getPw(),
            member.getNickname(),
            member.isSocial(),
            member.getMemberRoleList()
                  .stream()
                  .map(memberRole -> memberRole.name()).collect(Collectors.toList()));

    log.info(memberDTO);

    return memberDTO;


  }
  
}