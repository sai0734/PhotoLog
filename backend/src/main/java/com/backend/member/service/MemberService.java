package com.backend.member.service;

import com.backend.member.domain.Member;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import com.backend.member.dto.MemberDTO;
import com.backend.member.dto.MemberModifyDTO;

@Transactional
public interface MemberService {

    void modifyMember(MemberModifyDTO memberModifyDTO);

        default MemberDTO entityToDTO(Member member){
        
        MemberDTO dto = new MemberDTO(
            member.getEmail(),
             member.getPw(), 
             member.getNickname(), 
             member.isSocial(), 
             member.getMemberRoleList().stream()
             .map(memberRole -> memberRole.name()).collect(Collectors.toList()));
        return dto;
    }
}
