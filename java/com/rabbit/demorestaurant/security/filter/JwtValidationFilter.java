package com.rabbit.demorestaurant.security.filter;

import static com.rabbit.demorestaurant.security.TokenJwtConfig.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.rabbit.demorestaurant.security.SimpleGrantedAuthorityJsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    // PASAMOS EL AUTHENTICATIONMANAGER
    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // OBTENEMOS LAS CABECERAS
        String header = request.getHeader(HEADER_AUTHORIZATION);
        // VALIDAMOS SI NO TIENE TOKEN NOS SALIMOS 
        if (header == null ||  !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }
        // SI SI TIENE TOKEN: LE QUITAMOS LA PALABRA BEARER LA REMPLAZAMOS POR NADA 
        String token = header.replace(PREFIX_TOKEN, "");

        try {
            // VALIDAMOS EL TOKEN  CON LA LLAVE UNICA DEL TOKEN
            Claims claims = Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();
            // OBTENEMOS EL USERNAME 
            String username = claims.getSubject();
           // String username2 = (String) claims.get("username");
            // OBTENEMOS LOS ROLES COMO JSON
            Object authoritiesClaims = claims.get("authorities");
            // CONVERTIMOS ESE JSON DE LOS ROLES EN UN OBJETO DE TIPO COLLECTION 
            Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
            //
            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
            .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class) );
            // INICIAMOS SESION: CREAMOS EL TOKEN DE AUTHENTICATION 
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username, null,authorities);
            // NOS AUTHENTICAMOS 
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // ENVIAMOS UN TODO OK...
            chain.doFilter(request, response);
        } catch (Exception e) {
            // SI NO ES LA CLAVE DEL TOKEN MANDAMOS UN MENSAJE DE ERROR
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "el token jwt es invalido...");
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(CONTENT_TYPE);
        }

    }

    

}
