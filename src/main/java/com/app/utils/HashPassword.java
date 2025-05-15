package com.app.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashPassword {
    // Băm mật khẩu
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 là cost factor (có thể từ 10-14)
    }

    // Kiểm tra mật khẩu khi đăng nhập
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
