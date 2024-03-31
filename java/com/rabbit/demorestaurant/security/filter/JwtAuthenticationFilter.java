package com.rabbit.demorestaurant.security.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbit.demorestaurant.entities.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static com.rabbit.demorestaurant.security.TokenJwtConfig.*;

// FILTRO PARA AUTENTICAR Y GENERAR EL TOKEN 
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;




    // IMPLEMENTAMOS EL CONSTRUCTOR PARA IMPLEMETAR EL TRIBUTO
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    //FILTRO PARA AUTENTICARSE
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
                // Capturamos el json y lo convertimos en user: desearilizacion
                Users user = null;
                String username = null;
                String password = null;
                try {
                    // Poblamos los datos del json en el objeto user
                    user = new ObjectMapper().readValue(request.getInputStream(), Users.class);
                    username = user.getUsername();
                    password = user.getPassword();
                } catch (StreamReadException e) {
                    e.printStackTrace();
                } catch (DatabindException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // con esa informacion nos autenticamos
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
                    return authenticationManager.authenticate(authenticationToken);
            }

    //SI TODO SALE BIEN        
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
                // SE OBTIENE EL USUARIO
                org.springframework.security.core.userdetails.User user= 
                    (org.springframework.security.core.userdetails.User)authResult.getPrincipal();
                // SE OBTIENE EL USERNAME DEL USER
                String username = user.getUsername();
                // SE OBTINEN LOS ROLES 
                Collection<? extends GrantedAuthority> roles =  authResult.getAuthorities();
                Claims claims = Jwts.claims()
                    // SE OBTINEN LOS ROLES COMO UN JSON
                    .add("authorities", new ObjectMapper().writeValueAsString(roles))
                    .add("username",username)
                    .build();
                // SE GENERA EL TOKEN CON ESE USERNAME LA CONTRASEÑA SECRETA y los roles 
                String token = Jwts.builder()
                                   .subject(username)
                                   .claims(claims)
                                   // EL TOKEN SOLO VA A VALER UNA HORA Y LUEGO SE ELIMINA
                                   .expiration(new Date(System.currentTimeMillis() + 3600000)) 
                                   .signWith(SECRET_KEY)
                                   .compact();
                // SE DEVUELVE EL TOKEN AL CLIENTE OSEA A LA VISTA
                response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);
                // TAMB HAY Q PASARLO COMO UN JSON
                Map<String, String> body = new HashMap<>();
                body.put("token", token);
                body.put("username", username);
                //  body.put("message",String.format("Hola s% has iniciado session con exito",
                //   username));
                
                // ESCRIBIMOS ELJSON EN LA RESPUESTA
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                response.setContentType(CONTENT_TYPE);
                response.setStatus(200);
            }
    //SI TODO SALE mal
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "ERROR EN LA AUTETICACION USERNAME O PASSWORD INCORRECTOS!");
        body.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);
        response.setContentType(CONTENT_TYPE);
    }
    
    
}
