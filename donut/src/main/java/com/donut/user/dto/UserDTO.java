package com.donut.user.dto;

import com.donut.curriculum.dto.CurriculumDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@ToString
public class UserDTO {
    private String id;
    private String password;
    private String nickname;
    private String email;
    private LocalDate signupDate;
    private List<CurriculumDTO> curriculums;
}
