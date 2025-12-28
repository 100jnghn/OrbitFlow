package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

}
