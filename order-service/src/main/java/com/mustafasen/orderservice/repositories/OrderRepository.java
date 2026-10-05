package com.mustafasen.orderservice.repositories;

import com.mustafasen.orderservice.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findAllByOrderByCreatedAtDesc();
}
