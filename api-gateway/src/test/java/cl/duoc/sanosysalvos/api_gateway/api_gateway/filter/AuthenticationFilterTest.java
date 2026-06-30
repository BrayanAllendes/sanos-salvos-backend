package cl.duoc.sanosysalvos.api_gateway.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.Date;
import cl.duoc.sanosysalvos.api_gateway.filter.AuthenticationFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private GatewayFilterChain chain;
    private GatewayFilter gatewayFilter;

    // Clave secreta larga obligatoria de al menos 32 caracteres para JJWT
    private final String secretKeyFalsa = "ClaveSecretaParaPruebasUnitariasEnGateway123456"; 

    @BeforeEach
    void setUp() {
        filter = new AuthenticationFilter();
        // Inyectamos el valor del secret a la fuerza usando Reflection de Spring
        ReflectionTestUtils.setField(filter, "secret", secretKeyFalsa);
        
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty()); // Evitamos nulos reactivos
        
        gatewayFilter = filter.apply(new AuthenticationFilter.Config());
    }

    // Método de utilidad para fabricar tokens válidos en el test
    private String generarTokenFalso(String rol, Long id) {
        return Jwts.builder()
                .claim("rol", rol)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .signWith(Keys.hmacShaKeyFor(secretKeyFalsa.getBytes()))
                .compact();
    }

    @Test
    void testRutaPublica_BypassExitoso() {
        // Arrange: Simulamos ir a /api/usuarios/login sin token
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/usuarios/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert: Pasó directo al chain.filter sin modificar la respuesta a 401
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void testRutaProtegida_SinHeader_Retorna401() {
        // Arrange: Ruta protegida sin header de Authorization
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/mascotas").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void testRutaProtegida_TokenMalformado_Retorna401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/mascotas")
                .header(HttpHeaders.AUTHORIZATION, "Basic tokenraro")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testRutaProtegida_TokenFalso_Retorna401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/mascotas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer TokenQueNoEsValido")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testRutaDespachar_SinRolMunicipalidad_Retorna403() {
        // Arrange: Token válido pero con rol VECINO intentando entrar a /despachar
        String tokenVecino = generarTokenFalso("VECINO", 1L);
        MockServerHttpRequest request = MockServerHttpRequest.put("/api/mascotas/1/despachar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVecino)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert: Debe patearlo con 403 Forbidden
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void testRutaDespachar_ConRolMunicipalidad_Exitoso() {
        // Arrange: Token válido de la Muni entrando a /despachar
        String tokenMuni = generarTokenFalso("MUNICIPALIDAD", 2L);
        MockServerHttpRequest request = MockServerHttpRequest.put("/api/mascotas/1/despachar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMuni)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert
        verify(chain, times(1)).filter(any());
    }

    @Test
    void testRutaProtegida_PropagaHeadersInternos() {
        // Arrange: Vecino entrando a ver listar mascotas
        String tokenVecino = generarTokenFalso("VECINO", 10L);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/mascotas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVecino)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        gatewayFilter.filter(exchange, chain).block();

        // Assert: Verificamos que se añadieron los headers X-User-Role y X-User-Id
        verify(chain, times(1)).filter(argThat(mutatedExchange -> {
            HttpHeaders headers = mutatedExchange.getRequest().getHeaders();
            return "VECINO".equals(headers.getFirst("X-User-Role")) &&
                   "10".equals(headers.getFirst("X-User-Id"));
        }));
    }
}