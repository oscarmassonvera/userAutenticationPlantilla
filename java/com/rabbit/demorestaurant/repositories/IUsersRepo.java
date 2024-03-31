package com.rabbit.demorestaurant.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rabbit.demorestaurant.entities.Rol;
import com.rabbit.demorestaurant.entities.Users;

@Repository
public interface IUsersRepo extends CrudRepository<Users,Long> {
    Users findByUsername(String username);
    List<Users> findByRol(Rol rol);
}
