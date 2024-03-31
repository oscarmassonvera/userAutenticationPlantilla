package com.rabbit.demorestaurant.services;

import java.util.List;

import com.rabbit.demorestaurant.entities.Rol;
import com.rabbit.demorestaurant.entities.Users;

public interface IUserService {
    List<Users> findAll();
    Users saveUser(Users user);
    Users findByUsername(String username);
    List<Users> findByRol(Rol rol);
}
