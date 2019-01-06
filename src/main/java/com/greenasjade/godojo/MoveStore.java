package com.greenasjade.godojo;

import org.springframework.data.repository.CrudRepository;

public interface MoveStore extends CrudRepository<Move, Long> {
	public Move findByPlacement(String placement);
}