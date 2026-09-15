package com.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberModifyDTO {

    @NotBlank
    private String email;

    private String pw;

    @NotBlank
    private String nickname;
}