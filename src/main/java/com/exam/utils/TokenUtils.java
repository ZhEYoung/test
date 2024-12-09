package com.exam.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Token工具类
 */
@Component
public class TokenUtils {

    private static UserService staticUserService;

    @Resource
    private UserService userService;

    // Token密钥
    private static final String SECRET_KEY = "exam_system_secret_key";
    
    // Token过期时间（小时）
    private static final int EXPIRE_HOURS = 24;

    @PostConstruct
    public void setUserService() {
        staticUserService = userService;
    }

    /**
     * 生成token
     * @param userId 用户ID
     * @param sign 签名（通常使用密码的MD5值）
     * @return token字符串
     */
    public static String genToken(String userId, String sign) {
        return JWT.create()
                .withAudience(userId) // 将user id保存到token里面作为载荷
                .withIssuedAt(new Date()) // 设置token创建时间
                .withExpiresAt(DateUtil.offsetHour(new Date(), EXPIRE_HOURS)) // token过期时间
                .withClaim("sign", sign) // 添加签名作为自定义声明
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用密钥进行签名
    }

    /**
     * 获取当前登录的用户信息
     * @return 当前登录用户，如果未登录返回null
     */
    public static User getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.selectById(Integer.valueOf(userId));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 验证token是否有效
     * @param token JWT token
     * @return 如果有效返回true，否则返回false
     */
    public static boolean verify(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return false;
            }
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID，如果token无效返回null
     */
    public static Integer getUserId(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }
            DecodedJWT jwt = JWT.decode(token);
            String userId = jwt.getAudience().get(0);
            return Integer.valueOf(userId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断token是否过期
     * @param token JWT token
     * @return 如果过期返回true，否则返回false
     */
    public static boolean isExpired(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return true;
            }
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从请求头中获取token
     * @return token字符串，如果不存在返回null
     */
    public static String getTokenFromRequest() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader("token");
        } catch (Exception e) {
            return null;
        }
    }
} 