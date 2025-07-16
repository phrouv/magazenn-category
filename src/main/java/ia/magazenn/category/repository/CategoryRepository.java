/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import ia.magazenn.category.Category;

/**
 * Repository class for managing data operations on a {@link Category}.
 */
@ApplicationScoped
@WithSession
public class CategoryRepository implements PanacheRepositoryBase<Category, UUID> {

	public Uni<Category> findRandom() {
		return count().map(count -> (count > 0) ? count : null)
			.onItem()
			.ifNotNull()
			.transform(count -> new Random().nextInt(count.intValue()))
			.onItem()
			.ifNotNull()
			.transformToUni(randomCategory -> findAll().page(randomCategory, 1).firstResult());
	}

	public Uni<List<Category>> listAllWhereNameLike(String name) {
		return (name != null) ? list("LOWER(name) LIKE CONCAT('%', ?1, '%')", name.toLowerCase())
				: Uni.createFrom().item(List::of);
	}

}
