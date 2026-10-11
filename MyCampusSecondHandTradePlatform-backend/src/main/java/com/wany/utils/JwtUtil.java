package com.wany.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

/**
 * 获取用于签名的密钥
 * @return 返回一个HMAC-SHA算法的SecretKey对象，用于数字签名
 */
    private SecretKey getSigningKey() {
    // 使用预定义的secret字符串生成HMAC-SHA密钥
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

/**
 * 生成JWT令牌的方法
 * @param username 用户名
 * @param userId 用户ID
 * @return 生成的JWT令牌字符串
 */
    public String generateToken(String username, Long userId) {
    // 创建一个声明Map，用于存储JWT中的自定义数据
        Map<String, Object> claims = new HashMap<>();
    // 将用户ID添加到声明中
        claims.put("userId", userId);
    // 使用Jwts构建器创建JWT令牌
        return Jwts.builder()
            // 设置自定义声明
                .claims(claims)
            // 设置主题（通常为用户名）
                .subject(username)
            // 设置签发时间
                .issuedAt(new Date())
            // 设置过期时间（当前时间 + 有效期）
                .expiration(new Date(System.currentTimeMillis() + expiration))
            // 使用签名密钥进行签名
                .signWith(getSigningKey())
            // 压缩生成JWT字符串
                .compact();
    }

/**
 * 解析JWT令牌并返回其中的声明(Claims)信息
 * @param token JWT格式的令牌字符串
 * @return 解析后的Claims对象，包含令牌中的所有声明信息
 */
    public Claims parseToken(String token) {
    // 使用Jwts.parser()创建解析器
    // 设置验证密钥为通过getSigningKey()方法获取的密钥
    // 构建解析器
    // 解析签名后的令牌并获取负载(Claims)
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

/**
 * 从令牌中获取用户名
 * @param token JWT令牌字符串
 * @return 用户名
 */
    public String getUsernameFromToken(String token) {
    // 解析令牌并返回其主体（用户名）
        return parseToken(token).getSubject();
    }

/**
 * 从令牌中获取用户ID的方法
 * @param token 用户登录令牌
 * @return 用户ID(Long类型)
 */
    public Long getUserIdFromToken(String token) {
        // 解析令牌并获取其中的userId字段，返回Long类型
        return parseToken(token).get("userId", Long.class);
    }


/**
 * 检查给定的token是否已过期
 * @param token 需要检查的JWT token字符串
 * @return 如果token已过期或解析失败返回true，否则返回false
 */
    public Boolean isTokenExpired(String token) {
        try {
        // 解析token获取其中的声明信息
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
        // 如果解析过程中出现任何异常，视为token无效或已过期，返回true
            return true;
        }
    }

/**
 * 验证令牌的有效性
 * @param token 需要验证的令牌字符串
 * @param username 用户名，用于与令牌中的用户名进行比较
 * @return 如果令牌有效且用户名匹配则返回true，否则返回false
 */
    public Boolean validateToken(String token, String username) {
        try {
        // 从令牌中提取用户名
            String tokenUsername = getUsernameFromToken(token);
        // 比较令牌中的用户名与传入的用户名是否一致，并且检查令牌是否过期
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
        // 如果发生任何异常，返回false表示令牌无效
            return false;
        }
    }
}