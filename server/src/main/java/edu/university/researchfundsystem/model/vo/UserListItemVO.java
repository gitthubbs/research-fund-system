package edu.university.researchfundsystem.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserListItemVO {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String department;
    private String phone;
    private String email;
    private LocalDateTime createTime;
}
