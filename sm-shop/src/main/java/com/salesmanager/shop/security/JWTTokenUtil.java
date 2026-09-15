package com.salesmanager.shop.security;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.salesmanager.shop.security.user.JWTUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Lớp tiện ích quản lý tạo, giải mã, kiểm tra và làm mới JWT Token
 */
@Component
public class JWTTokenUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    // Khoảng thời gian ân hạn (giây) cho việc refresh token
    private static final int GRACE_PERIOD_SECONDS = 200;

    @Value("${jwt.secret:defaultSecretKeyNeedToBeLongEnoughForHMACSHA512AlgorithmSecurityStandard}")
    private String secret;

    @Value("${jwt.expiration:86400}") // Mặc định 24h (tính bằng giây)
    private Long expiration;

    /**
     * Tạo khóa bảo mật HMAC-SHA từ secret string
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- CÁC HÀM TRÍCH XUẤT THÔNG TIN TỪ TOKEN ---

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // --- CÁC HÀM KIỂM TRA HẠN TOKEN ---

    public Boolean isTokenExpired(String token) {
        final Date exp = getExpirationDateFromToken(token);
        return exp.before(new Date());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    // --- CÁC HÀM TẠO VÀ LÀM MỚI TOKEN ---

    /**
     * Sinh token mới từ thông tin người dùng đăng nhập
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant expInstant = now.plus(expiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expInstant))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Làm mới token (gia hạn thêm thời gian sống)
     */
    public String refreshToken(String token) {
        Instant now = Instant.now();
        Instant expInstant = now.plus(expiration, ChronoUnit.SECONDS);

        final Claims claims = getAllClaimsFromToken(token);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expInstant))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Kiểm tra xem token có đủ điều kiện để refresh hay không
     */
    public Boolean canTokenBeRefreshedWithGrace(String token, Date lastPasswordReset) {
        Date expirationDate = getExpirationDateFromToken(token);
        // Cho phép ân hạn thêm GRACE_PERIOD_SECONDS sau khi hết hạn
        Date graceExpiration = Date.from(expirationDate.toInstant().plus(GRACE_PERIOD_SECONDS, ChronoUnit.SECONDS));
        
        boolean isNotExpiredWithGrace = new Date().before(graceExpiration);
        Date created = getIssuedAtDateFromToken(token);
        boolean isNotResetBefore = lastPasswordReset == null || created.after(lastPasswordReset);

        return isNotExpiredWithGrace && isNotResetBefore;
    }

    /**
     * Xác thực token có hợp lệ với User hiện tại không
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        if (!(userDetails instanceof JWTUser user)) {
            return false;
        }
        final String username = getUsernameFromToken(token);
        final Date created = getIssuedAtDateFromToken(token);

        boolean usernameEquals = username.equals(user.getUsername());
        boolean tokenNotExpired = !isTokenExpired(token);
        boolean notCreatedBeforePwdReset = !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate());

        return (usernameEquals && tokenNotExpired && notCreatedBeforePwdReset);
    }
}