package cl.duoc.sanosysalvos.ms_usuarios.security;
 
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import java.security.Key;
import java.util.Date;
 
@Component
public class JwtUtil {
 
    @Value("${jwt.secret}")
    private String secret;
 
    private final long expirationTime = 3600000; // 1 hora
 
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
 
    /**
     * FIX CRÍTICO: Se agregan los claims 'rol' e 'id' al payload del JWT.
     *
     * Antes, el token solo guardaba el email como subject. Esto impedía que
     * el API Gateway leyera el rol del usuario y aplicara control de acceso
     * sin tener que hacer una llamada extra a ms-usuarios en cada request.
     *
     * Ahora el payload del JWT tiene la forma:
     *   { "sub": "vecino@vecino.cl", "rol": "VECINO", "id": 1, "iat": ..., "exp": ... }
     *
     * @param email Email del usuario autenticado (subject estándar JWT)
     * @param rol   Rol de negocio: VECINO, VETERINARIO o MUNICIPALIDAD
     * @param id    PK de la tabla usuarios en PostgreSQL
     */
    public String generarToken(String email, String rol, Long id) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}