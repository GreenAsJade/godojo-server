package com.greenasjade.j01;

import org.springframework.data.repository.CrudRepository;

public interface MoveNodeStore extends CrudRepository<MoveNode, Long> {
	MoveNode findByPlay(String play);
}