package com.fastfood.fast_food_backend.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.dto.LoginResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final String PREFIJO_ROL = "ROLE_";
    private static final String ALGORITMO = "HmacSHA256";
    private static final Set<String> ROLES_PERMITIDOS = Set.of("CLIENTE", "EMPLEADO", "ADMINISTRADOR");
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private final String issuer;
    private final Duration duracion;

    public JwtService(
            SecretKey secretKey,
            ObjectMapper objectMapper,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MINUTES debe ser mayor que cero");
        }
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
        this.issuer = issuer;
        this.duracion = Duration.ofMinutes(expirationMinutes);
    }

    public LoginResponse generar(Authentication authentication) {
        Instant emitido = Instant.now();
        Instant expira = emitido.plus(duracion);
        String rol = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith(PREFIJO_ROL))
                .map(authority -> authority.substring(PREFIJO_ROL.length()))
                .filter(ROLES_PERMITIDOS::contains)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene un rol válido"));

        String token = generarToken(authentication.getName(), rol, emitido, expira);
        return new LoginResponse(token, duracion.toSeconds(), authentication.getName(), rol);
    }

    String generarToken(String correo, String rol, Instant emitido, Instant expira) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("iss", issuer);
            payload.put("sub", correo);
            payload.put("rol", rol);
            payload.put("iat", emitido.getEpochSecond());
            payload.put("exp", expira.getEpochSecond());

            String headerCodificado = codificarJson(header);
            String payloadCodificado = codificarJson(payload);
            String contenidoFirmado = headerCodificado + "." + payloadCodificado;
            return contenidoFirmado + "." + BASE64_URL_ENCODER.encodeToString(firmar(contenidoFirmado));
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible generar el token JWT", exception);
        }
    }

    public JwtClaims validarYLeer(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                throw new IllegalArgumentException("El token JWT no tiene un formato válido");
            }

            String contenidoFirmado = partes[0] + "." + partes[1];
            byte[] firmaRecibida = BASE64_URL_DECODER.decode(partes[2]);
            byte[] firmaEsperada = firmar(contenidoFirmado);
            if (!MessageDigest.isEqual(firmaEsperada, firmaRecibida)) {
                throw new IllegalArgumentException("La firma del token JWT no es válida");
            }

            JsonNode header = objectMapper.readTree(BASE64_URL_DECODER.decode(partes[0]));
            if (!"HS256".equals(header.path("alg").asText())) {
                throw new IllegalArgumentException("El algoritmo del token JWT no está permitido");
            }

            JsonNode payload = objectMapper.readTree(BASE64_URL_DECODER.decode(partes[1]));
            String tokenIssuer = payload.path("iss").asText();
            String correo = payload.path("sub").asText();
            String rol = payload.path("rol").asText();
            long emitido = payload.path("iat").asLong(0);
            long expira = payload.path("exp").asLong(0);
            long ahora = Instant.now().getEpochSecond();

            if (!issuer.equals(tokenIssuer)) {
                throw new IllegalArgumentException("El emisor del token JWT no es válido");
            }
            if (correo.isBlank() || !ROLES_PERMITIDOS.contains(rol)) {
                throw new IllegalArgumentException("El token JWT no contiene una identidad válida");
            }
            if (expira <= ahora) {
                throw new IllegalArgumentException("El token JWT está vencido");
            }
            if (emitido <= 0 || emitido > ahora + 60) {
                throw new IllegalArgumentException("La fecha de emisión del token JWT no es válida");
            }

            return new JwtClaims(correo, rol, expira);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("El token JWT no es válido", exception);
        }
    }

    private String codificarJson(Map<String, Object> datos) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(datos));
    }

    private byte[] firmar(String contenido) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(ALGORITMO);
        mac.init(secretKey);
        return mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
    }

    public record JwtClaims(String correo, String rol, long expiraEnEpochSeconds) {
    }
}
