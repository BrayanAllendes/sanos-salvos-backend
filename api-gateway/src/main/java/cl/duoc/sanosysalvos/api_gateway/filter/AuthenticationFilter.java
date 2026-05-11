package cl.duoc.sanosysalvos.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Verificar si existe el encabezado de Autorización
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleException(exchange, "Falta token de autorización", HttpStatus.UNAUTHORIZED);
            }

            // 2. Usar getFirst() para evitar el aviso de "Potential null pointer"
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            // 3. Verificar que el formato sea "Bearer [token]"
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleException(exchange, "Formato de token inválido", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 4. USO DE JWTS: Validamos el token para que el import no esté amarillo
                Jwts.parserBuilder().build().parseClaimsJws(token);
                
            } catch (Exception e) {
                // Si el token es inválido o expiró
                return handleException(exchange, "Token no válido o expirado", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> handleException(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Clase necesaria para la configuración del filtro
    }
}