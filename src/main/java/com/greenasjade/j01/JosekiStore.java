package com.greenasjade.j01;

import org.springframework.data.repository.CrudRepository;

public interface JosekiStore extends CrudRepository<Joseki, Long> {
	Joseki findByName(String name);
}