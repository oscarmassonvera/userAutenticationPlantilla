package com.rabbit.demorestaurant.services;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rabbit.demorestaurant.entities.Rol;
import com.rabbit.demorestaurant.entities.Users;
import com.rabbit.demorestaurant.repositories.IUsersRepo;

@Service
public class JpaUserDetailsService  implements UserDetailsService{

    @Autowired
    private IUsersRepo repository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // BUSCA EL USERNAME EN EL REPOSITORIO
        Users user = repository.findByUsername(username);
        
        // VALIDA SI EL USUARIO EXISTE
        if (user == null) {
            // SI EL USUARIO NO EXISTE, LANZAR UNA EXCEPCIÓN
            throw new UsernameNotFoundException(String.format("El usuario '%s' no existe", username));
        }
        
        // OBTENEMOS LOS ROLES
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRol() == Rol.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user.getRol() == Rol.WAITER) {
            authorities.add(new SimpleGrantedAuthority("ROLE_WAITER"));
        }
    
        // RETORNAR UN OBJETO USERDETAILS CON LA INFORMACIÓN DEL USUARIO
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            true, // CUENTA NO CADUCADA
            true, // CREDENCIALES NO CADUCADAS
            true, // CUENTA NO BLOQUEADA
            authorities
        );
    }

}
