package com.donut.user;

import com.donut.curriculum.CurriculumDTO;
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
