package com.backend.mapper;

import com.backend.member.mapper.MemberMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.member.domain.Member;
import com.backend.member.domain.MemberRole;

@SpringBootTest
@Log4j2
public class MemberMapperTests {

  @Autowired
  private MemberMapper memberMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  public void testInsertMember(){

    for (int i = 0; i < 10 ; i++) {

      String email = "user"+i+"@aaa.com";

      Member member = Member.builder()
              .email(email)
              .pw(passwordEncoder.encode("1111"))
              .nickname("USER"+i)
              .build();

      member.addRole(MemberRole.USER);

      if(i >= 5){
          member.addRole(MemberRole.MANAGER);
      }

      if(i >=8){
          member.addRole(MemberRole.ADMIN);
      }

      memberMapper.insert(member);
      member.getMemberRoleList().forEach(role -> memberMapper.insertRole(email, role.name()));
    }
  }

  @Test
  public void testRead() {

    String email = "user9@aaa.com";

    Member member = memberMapper.selectByEmail(email);

    log.info("-----------------");
    log.info(member);
  }

}
