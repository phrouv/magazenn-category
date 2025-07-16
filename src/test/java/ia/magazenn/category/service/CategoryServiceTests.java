/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import ia.magazenn.category.Category;
import ia.magazenn.category.mapping.CaregoryFullUpdateMapper;
import ia.magazenn.category.mapping.CategoryPartialUpdateMapper;
import ia.magazenn.category.repository.CategoryRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class CategoryServiceTests {

	private static final String DEFAULT_NAME = "Super Chocolatine";

	private static final String UPDATED_NAME = DEFAULT_NAME + " (updated)";

	private static final String DEFAULT_DESCRIPTION = "Super Chocolatine chocolate in";

	private static final String UPDATED_DESCRIPTION = DEFAULT_DESCRIPTION + " (updated)";

	private static final UUID DEFAULT_ID = UUID.fromString("68e78a3a-853e-4875-af34-d17f8af52eab");

	private static final UUID UPDATED_ID = UUID.fromString("ca72748e-757b-4bf0-b736-d01226615b07");

	@Inject
	CategoryService categoryService;

	@InjectMock
	CategoryRepository categoryRepository;

	@InjectSpy
	CategoryPartialUpdateMapper categoryPartialUpdateMapper;

	@InjectSpy
	CaregoryFullUpdateMapper caregoryFullUpdateMapper;

	@Test
	void findAllCategoriesNoneFound() {
		when(this.categoryRepository.listAll()).thenReturn(Uni.createFrom().item(List.of()));

		var allCategories = this.categoryService.findAllCategories()
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(allCategories).isNotNull().isEmpty();

		verify(this.categoryRepository).listAll();
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findAllCategories() {
		when(this.categoryRepository.listAll()).thenReturn(Uni.createFrom().item(List.of(createDefaultCategory())));

		var allCategories = this.categoryService.findAllCategories()
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(allCategories).isNotNull()
			.isNotEmpty()
			.singleElement()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryRepository).listAll();
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@ParameterizedTest(
			name = DISPLAY_NAME_PLACEHOLDER + "[" + INDEX_PLACEHOLDER + "] (" + ARGUMENTS_WITH_NAMES_PLACEHOLDER + ")")
	@ValueSource(strings = { "name" })
	@NullSource
	void findAllCategoriesHavingNameNoneFound(String name) {
		when(this.categoryRepository.listAllWhereNameLike(eq(name))).thenReturn(Uni.createFrom().item(List.of()));

		var allCategories = this.categoryService.findAllCategoriesHavingName(name)
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(allCategories).isNotNull().isEmpty();

		verify(this.categoryRepository).listAllWhereNameLike(eq(name));
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findAllCategoriesHavingName() {
		when(this.categoryRepository.listAllWhereNameLike(eq("name")))
			.thenReturn(Uni.createFrom().item(List.of(createDefaultCategory())));

		var allCategories = this.categoryService.findAllCategoriesHavingName("name")
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(allCategories).isNotNull()
			.isNotEmpty()
			.hasSize(1)
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(tuple(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION));

		verify(this.categoryRepository).listAllWhereNameLike(eq("name"));
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findCategoryByIdFound() {
		when(this.categoryRepository.findById(eq(DEFAULT_ID)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var category = this.categoryService.findCategoryById(DEFAULT_ID)
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(category).isNotNull()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryRepository).findById(eq(DEFAULT_ID));
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findCategoryByIdNotFound() {
		when(this.categoryRepository.findById(eq(DEFAULT_ID))).thenReturn(Uni.createFrom().nullItem());

		var category = this.categoryService.findCategoryById(DEFAULT_ID)
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(category).isNull();

		verify(this.categoryRepository).findById(eq(DEFAULT_ID));
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findRandomCategoryNotFound() {
		when(this.categoryRepository.findRandom()).thenReturn(Uni.createFrom().nullItem());

		var randomCategory = this.categoryService.findRandomCategory()
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(randomCategory).isNull();

		verify(this.categoryRepository).findRandom();
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	void findRandomCategoryFound() {
		when(this.categoryRepository.findRandom()).thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var randomCategory = this.categoryService.findRandomCategory()
			.subscribe()
			.withSubscriber(UniAssertSubscriber.create())
			.assertSubscribed()
			.awaitItem(Duration.ofSeconds(5))
			.getItem();

		assertThat(randomCategory).isNotNull()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryRepository).findRandom();
		verifyNoMoreInteractions(this.categoryRepository);
	}

	@Test
	@RunOnVertxContext
	void persistNullCategory(UniAsserter asserter) {
		asserter.assertFailedWith(() -> this.categoryService.persistCategory(null), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly(null, "ne doit pas être nul");

			verifyNoInteractions(this.categoryRepository);
		});
	}

	@Test
	@RunOnVertxContext
	void persistInvalidCategory(UniAsserter asserter) {
		var category = createDefaultCategory();
		category.setName("a");

		asserter.assertFailedWith(() -> this.categoryService.persistCategory(category), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly("a", "la taille doit être comprise entre 3 et 50");

			verifyNoInteractions(this.categoryRepository);
		});
	}

	@Test
	@RunOnVertxContext
	void persistCategory(UniAsserter asserter) {
		when(this.categoryRepository.persist(any(Category.class)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var categoryToPersist = createDefaultCategory();
		categoryToPersist.setId(null);

		asserter.assertThat(() -> this.categoryService.persistCategory(categoryToPersist), persistedCategory -> {
			assertThat(persistedCategory).isNotNull()
				.extracting(Category::getId, Category::getName, Category::getDescription)
				.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

			verify(this.categoryRepository).persist(any(Category.class));
			verifyNoMoreInteractions(this.categoryRepository);
		});
	}

	@Test
	@RunOnVertxContext
	void fullyUpdateNullCategory(UniAsserter asserter) {

		System.setProperty("user.language", "en");
		asserter.assertFailedWith(() -> this.categoryService.replaceCategory(null), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly(null, "ne doit pas être nul");

			verifyNoInteractions(this.categoryRepository, this.caregoryFullUpdateMapper,
					this.categoryPartialUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void fullyUpdateInvalidCategory(UniAsserter asserter) {
		var category = createDefaultCategory();
		category.setName(null);

		asserter.assertFailedWith(() -> this.categoryService.replaceCategory(category), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly(null, "ne doit pas être nul");

			verifyNoInteractions(this.categoryRepository, this.caregoryFullUpdateMapper,
					this.categoryPartialUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void fullyUpdateNotFoundCategory(UniAsserter asserter) {
		when(this.categoryRepository.findById(eq(DEFAULT_ID))).thenReturn(Uni.createFrom().nullItem());

		asserter.assertThat(() -> this.categoryService.replaceCategory(createUpdatedCategory()), category -> {
			assertThat(category).isNull();

			verify(this.categoryRepository).findById(eq(DEFAULT_ID));
			verifyNoMoreInteractions(this.categoryRepository);
			verifyNoInteractions(this.categoryPartialUpdateMapper, this.caregoryFullUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void fullyUpdateCategory(UniAsserter asserter) {
		when(this.categoryRepository.findById(eq(DEFAULT_ID)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		asserter.assertThat(() -> this.categoryService.replaceCategory(createUpdatedCategory()), replacedCategory -> {
			assertThat(replacedCategory).isNotNull()
				.extracting(Category::getId, Category::getName, Category::getDescription)
				.containsExactly(DEFAULT_ID, UPDATED_NAME, UPDATED_DESCRIPTION);

			verify(this.categoryRepository).findById(eq(DEFAULT_ID));
			verifyNoMoreInteractions(this.categoryRepository);
			verify(this.caregoryFullUpdateMapper).mapFullUpdate(any(Category.class), any(Category.class));
			verifyNoInteractions(this.categoryPartialUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void partiallyUpdateNullCategory(UniAsserter asserter) {
		asserter.assertFailedWith(() -> this.categoryService.partialUpdateCategory(null), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly(null, "ne doit pas être nul");

			verifyNoInteractions(this.categoryRepository, this.caregoryFullUpdateMapper,
					this.categoryPartialUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void partiallyUpdateInvalidCategory(UniAsserter asserter) {
		when(this.categoryRepository.findById(eq(DEFAULT_ID)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var category = createDefaultCategory();
		category.setName("a");

		asserter.assertFailedWith(() -> this.categoryService.partialUpdateCategory(category), cve -> {
			assertThat(cve).isNotNull().isInstanceOf(ConstraintViolationException.class);

			var violations = ((ConstraintViolationException) cve).getConstraintViolations();

			assertThat(violations).isNotNull()
				.singleElement()
				.isNotNull()
				.extracting(ConstraintViolation::getInvalidValue, ConstraintViolation::getMessage)
				.containsExactly("a", "la taille doit être comprise entre 3 et 50");

			verify(this.categoryRepository).findById(eq(DEFAULT_ID));
			verifyNoMoreInteractions(this.categoryRepository);
			verify(this.categoryPartialUpdateMapper).mapPartialUpdate(any(Category.class), any(Category.class));
			verifyNoInteractions(this.caregoryFullUpdateMapper);
		});
	}

	@Test
	@RunOnVertxContext
	void partiallyUpdateNotFoundCategory(UniAsserter asserter) {
		when(this.categoryRepository.findById(eq(DEFAULT_ID))).thenReturn(Uni.createFrom().nullItem());

		asserter.assertThat(() -> this.categoryService.partialUpdateCategory(createPartialUpdatedCategory()),
				category -> {
					assertThat(category).isNull();

					verify(this.categoryRepository).findById(eq(DEFAULT_ID));
					verifyNoMoreInteractions(this.categoryRepository);
					verifyNoInteractions(this.caregoryFullUpdateMapper, this.categoryPartialUpdateMapper);
				});
	}

	@Test
	@RunOnVertxContext
	void partiallyUpdateCategory(UniAsserter asserter) {
		when(this.categoryRepository.findById(eq(DEFAULT_ID)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		asserter.assertThat(() -> this.categoryService.partialUpdateCategory(createPartialUpdatedCategory()),
				category -> {
					assertThat(category).isNotNull()
						.extracting(Category::getId, Category::getName, Category::getDescription)
						.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

					verify(this.categoryRepository).findById(eq(DEFAULT_ID));
					verifyNoMoreInteractions(this.categoryRepository);
					verify(this.categoryPartialUpdateMapper).mapPartialUpdate(any(Category.class), any(Category.class));
					verifyNoInteractions(this.caregoryFullUpdateMapper);
				});
	}

	@Test
	@RunOnVertxContext
	void deleteCategory(UniAsserter asserter) {
		when(this.categoryRepository.deleteById(eq(DEFAULT_ID))).thenReturn(Uni.createFrom().item(true));

		asserter.assertThat(() -> this.categoryService.deleteCategory(DEFAULT_ID), v -> {
			verify(this.categoryRepository).deleteById(eq(DEFAULT_ID));
			verifyNoMoreInteractions(this.categoryRepository);
		});
	}

	@Test
	@RunOnVertxContext
	void deleteAllCategories(UniAsserter asserter) {
		var h1 = createDefaultCategory();
		var h2 = createUpdatedCategory();
		h2.setId(UPDATED_ID);

		when(this.categoryRepository.deleteById(any(UUID.class))).thenReturn(Uni.createFrom().item(true));

		when(this.categoryRepository.listAll()).thenReturn(Uni.createFrom().item(List.of(h1, h2)));

		asserter.assertThat(() -> this.categoryService.deleteAllCategories(), v -> {
			verify(this.categoryRepository).listAll();
			verify(this.categoryRepository).deleteById(h1.getId());
			verify(this.categoryRepository).deleteById(h2.getId());
			verifyNoMoreInteractions(this.categoryRepository);
		});
	}

	@Test
	@RunOnVertxContext
	void replaceAllCategories(UniAsserter asserter) {
		var h1 = createDefaultCategory();
		var h2 = createUpdatedCategory();
		h2.setId(UPDATED_ID);

		var categories = List.of(createDefaultCategory(), createPartialUpdatedCategory());
		categories.forEach(h -> h.setId(null));

		when(this.categoryRepository.deleteById(any(UUID.class))).thenReturn(Uni.createFrom().item(true));

		when(this.categoryRepository.listAll()).thenReturn(Uni.createFrom().item(List.of(h1, h2)));

		when(this.categoryRepository.persist(anyIterable())).thenReturn(Uni.createFrom().voidItem());

		asserter.assertThat(() -> this.categoryService.replaceAllCategories(categories), v -> {
			verify(this.categoryRepository).listAll();
			verify(this.categoryRepository).deleteById(eq(h1.getId()));
			verify(this.categoryRepository).deleteById(eq(h2.getId()));
			verify(this.categoryRepository).persist(anyIterable());
			verifyNoMoreInteractions(this.categoryRepository);
		});
	}

	private static Category createDefaultCategory() {
		Category category = new Category();
		category.setId(DEFAULT_ID);
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);
		return category;
	}

	public static Category createUpdatedCategory() {
		Category category = createDefaultCategory();
		category.setName(UPDATED_NAME);
		category.setDescription(UPDATED_DESCRIPTION);
		return category;
	}

	public static Category createPartialUpdatedCategory() {
		Category category = createDefaultCategory();
		return category;
	}

}
