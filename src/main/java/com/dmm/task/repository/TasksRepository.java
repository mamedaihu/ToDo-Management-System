package com.dmm.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dmm.task.enttity.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Integer>, JpaSpecificationExecutor<Tasks>{
	

}
