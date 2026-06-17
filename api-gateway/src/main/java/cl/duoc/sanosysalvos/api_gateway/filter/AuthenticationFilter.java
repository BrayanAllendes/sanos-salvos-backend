package cl.duoc.sanosysalvos.api_gateway.filter;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
 
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
 
    @Value("${jwt.secret}")
    private String secret;
 
    // Rutas completamente públicas (no requieren token)
    private static final java.util.List<String> RUTAS_PUBLICAS = java.util.List.of(
        "/api/usuarios/login",
        "/api/usuarios/registro"
    );
 
    // Sufijo de ruta protegida exclusivamente para MUNICIPALIDAD
    private static final String SUFIJO_DESPACHAR = "/despachar";
    private static final String ROL_MUNICIPALIDAD = "MUNICIPALIDAD";
 
    public AuthenticationFilter() {
        super(Config.class);
    }
 
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
 
            // ── 1. Bypass para rutas públicas ──────────────────────────────────
            boolean esPublica = RUTAS_PUBLICAS.stream().anyMatch(path::startsWith);
            if (esPublica) {
                return chain.filter(exchange);
            }
 
            // ── 2. Verificar presencia y formato del header Authorization ──────
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleException(exchange, HttpStatus.UNAUTHORIZED);
            }
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleException(exchange, HttpStatus.UNAUTHORIZED);
            }
 
            String token = authHeader.substring(7);
 
            try {
                // ── 3. Validar firma y expiración del JWT ──────────────────────
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
 
                // ── 4. Extraer claims embebidos en el payload ──────────────────
                String userRole = claims.get("rol", String.class);
 
                // El claim "id" puede llegar como Integer o Long según JJWT
                Object idObj = claims.get("id");
                String userId = idObj != null ? String.valueOf(idObj) : "";
 
                // ── 5. CONTROL DE ACCESO POR ROL ───────────────────────────────
                // PUT /api/mascotas/{id}/despachar → solo MUNICIPALIDAD.
                // El check es sobre el SUFIJO de la URL para no acoplar el filtro
                // a una ruta específica. Si en el futuro se agrega
                // /api/refugios/{id}/despachar, queda protegido automáticamente.
                if (path.endsWith(SUFIJO_DESPACHAR) && !ROL_MUNICIPALIDAD.equals(userRole)) {
                    return handleException(exchange, HttpStatus.FORBIDDEN);
                }
 
                // ── 6. Propagar identidad downstream ───────────────────────────
                // Los microservicios reciben el rol y el id del usuario autenticado
                // como headers internos. Esto evita que cada servicio tenga que
                // re-parsear o re-validar el JWT por su cuenta.
                ServerHttpRequest requestEnriquecido = request.mutate()
                        .header("X-User-Role", userRole != null ? userRole : "")
                        .header("X-User-Id",   userId)
                        .build();
 
                return chain.filter(exchange.mutate().request(requestEnriquecido).build());
 
            } catch (Exception e) {
                // Token malformado, firma inválida o token expirado
                return handleException(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }
 
    private Mono<Void> handleException(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
 
    public static class Config {
        // Clase de configuración requerida por AbstractGatewayFilterFactory
    }
}