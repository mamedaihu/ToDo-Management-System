package com.dmm.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.enttity.Users;

public interface UsersRepository extends JpaRepository<Users, String>{

}
