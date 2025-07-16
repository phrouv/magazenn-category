/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.repository;

import io.quarkus.logging.Log;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ia.magazenn.category.Category;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestReactiveTransaction
class CategoryRepositoryTests {

	private static final String DEFAULT_NAME = "Super Chocolatine";

	private static final String DEFAULT_DESCRIPTION = "Super Chocolatine chocolate in";

	@Inject
	CategoryRepository categoryRepository;

	@Test
	void findAllWhereNameLikeFound(UniAsserter asserter) {
		// Doing it this way because UniAsserter doesn't work well with ParameterizedTest
		var names = Stream.of(DEFAULT_NAME, "choco", "Choco", "CHOCO", "Chocolatine", "super", "l", "");

		Category category = new Category();
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		asserter.execute(this.categoryRepository::deleteAll)
			.assertEquals(this.categoryRepository::count, 0L)
			.execute(() -> this.categoryRepository.persist(category))
			.assertEquals(this.categoryRepository::count, 1L);

		names.forEach(name -> asserter.execute(() -> Log.infof("Inside listAllWhereNameLike(%s)", name))
			.assertThat(() -> this.categoryRepository.listAllWhereNameLike(name),
					categories -> assertThat(categories).isNotNull()
						.hasSize(1)
						.first()
						.usingRecursiveComparison()
						.isEqualTo(category)));
	}

	@Test
	void findAllWhereNameLikeNotFound(UniAsserter asserter) {
		// Doing it this way because UniAsserter doesn't work well with ParameterizedTest
		var names = Stream.of("v", "support", "chocolate", null);

		Category category = new Category();
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		asserter.execute(this.categoryRepository::deleteAll)
			.assertEquals(this.categoryRepository::count, 0L)
			.execute(() -> this.categoryRepository.persist(category))
			.assertEquals(this.categoryRepository::count, 1L);

		names.forEach(name -> asserter.execute(() -> Log.infof("Inside findAllWhereNameLikeNotFound(%s)", name))
			.assertThat(() -> this.categoryRepository.listAllWhereNameLike(name),
					categories -> assertThat(categories).isNotNull().isEmpty()));
	}

}
