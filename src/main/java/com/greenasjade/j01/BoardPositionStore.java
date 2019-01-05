package com.greenasjade.j01;

import org.springframework.data.repository.CrudRepository;

public interface BoardPositionStore extends CrudRepository<BoardPosition, Long> {
	BoardPosition findByPlay(String play);
}