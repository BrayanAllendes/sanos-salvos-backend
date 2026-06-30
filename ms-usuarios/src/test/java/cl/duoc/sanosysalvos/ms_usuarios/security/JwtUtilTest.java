package cl.duoc.sanosysalvos.ms_usuarios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    
    // Usamos la misma clave de tu application.properties
    private final String SECRET_KEY = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inyectamos la clave secreta manualmente
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
    }

    @Test
    void testGenerarToken_Exitoso() {
        // Arrange
        String email = "vecino@test.cl";
        String rol = "VECINO";
        Long id = 1L;

        // Act
        String token = jwtUtil.generarToken(email, rol, id);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerarToken_ContieneClaimsCorrectos() {
        // Arrange
        String email = "veterinario@test.cl";
        String rol = "VETERINARIO";
        Long id = 99L;

        // Act
        String token = jwtUtil.generarToken(email, rol, id);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(email, claims.getSubject());
        assertEquals(rol, claims.get("rol", String.class));
        assertEquals(id, claims.get("id", Long.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}