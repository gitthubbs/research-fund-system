package edu.university.researchfundsystem.model.vo;

import lombok.Data;

@Data
public class LoginResponseVO {
    private String token;
    private String role;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String name;
        private String role;
        private String department;
        private String phone;
        private String email;
    }
}
