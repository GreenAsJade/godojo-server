package com.greenasjade.j01;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface BoardPositionStore extends CrudRepository<BoardPosition, Long> {
	BoardPosition findByPlay(String play);
	Optional<BoardPosition> findById(Long id);
}