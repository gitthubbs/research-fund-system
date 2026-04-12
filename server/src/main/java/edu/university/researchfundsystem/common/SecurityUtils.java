package edu.university.researchfundsystem.common;

import javax.servlet.http.HttpServletRequest;

public class SecurityUtils {

    /**
     * 从请求头中提取当前用户ID
     * Token 格式约定为 "token-{userId}"，由前端放在 Authorization: Bearer {token} 中
     * @param request HTTP请求
     * @return 用户ID，提取失败返回 null
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = authHeader.substring(7); // 去掉 "Bearer "
            if (token.startsWith("token-")) {
                String idStr = token.substring(6); // 去掉 "token-"
                return Long.valueOf(idStr);
            }
        } catch (Exception e) {
            // 解析失败
        }
        return null;
    }
}
