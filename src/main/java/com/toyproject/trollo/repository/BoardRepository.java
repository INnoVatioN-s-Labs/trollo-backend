package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
