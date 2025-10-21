package com.dw.admin.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 */
public class PasswordUtils {
    
    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * 验证密码
     * @param password 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String password, String encodedPassword) {
        return BCrypt.checkpw(password, encodedPassword);
    }
}