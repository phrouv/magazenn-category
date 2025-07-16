/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.service;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import ia.magazenn.category.Category;
import ia.magazenn.category.mapping.CaregoryFullUpdateMapper;
import ia.magazenn.category.mapping.CategoryPartialUpdateMapper;
import ia.magazenn.category.repository.CategoryRepository;

/**
 * Service class containing business methods for the application.
 */
@ApplicationScoped
public class CategoryService {

	private final CategoryRepository categoryRepository;

	private final Validator validator;

	private final CategoryPartialUpdateMapper categoryPartialUpdateMapper;

	private final CaregoryFullUpdateMapper caregoryFullUpdateMapper;

	public CategoryService(CategoryRepository categoryRepository, Validator validator,
			CategoryPartialUpdateMapper categoryPartialUpdateMapper,
			CaregoryFullUpdateMapper caregoryFullUpdateMapper) {
		this.categoryRepository = categoryRepository;
		this.validator = validator;
		this.categoryPartialUpdateMapper = categoryPartialUpdateMapper;
		this.caregoryFullUpdateMapper = caregoryFullUpdateMapper;
	}

	@WithSpan("CategoryService.findAllCategories")
	public Uni<List<Category>> findAllCategories() {
		Log.debug("Getting all categories");
		return this.categoryRepository.listAll();
	}

	@WithSpan("CategoryService.findAllCategoriesHavingName")
	public Uni<List<Category>> findAllCategoriesHavingName(@SpanAttribute("arg.name") String name) {
		Log.debugf("Finding all categories having name = %s", name);
		return this.categoryRepository.listAllWhereNameLike(name);
	}

	@WithSpan("CategoryService.findCategoryById")
	public Uni<Category> findCategoryById(@SpanAttribute("arg.id") UUID id) {
		Log.debugf("Finding category by id = %s", id);
		try {
			return this.categoryRepository.findById(id);
		}
		catch (IllegalArgumentException exc) {
			return null;
		}
	}

	@WithSpan("CategoryService.findRandomCategory")
	public Uni<Category> findRandomCategory() {
		Log.debug("Finding a random category");
		return this.categoryRepository.findRandom();
	}

	@WithSpan("CategoryService.persistCategory")
	@WithTransaction
	public Uni<Category> persistCategory(@SpanAttribute("arg.category") @NotNull @Valid Category category) {
		Log.debugf("Persisting category: %s", category);
		return this.categoryRepository.persist(category);
	}

	@WithSpan("CategoryService.replaceCategory")
	@WithTransaction
	public Uni<Category> replaceCategory(@SpanAttribute("arg.category") @NotNull @Valid Category category) {
		Log.debugf("Replacing category: %s", category);
		return this.categoryRepository.findById(category.getId()).onItem().ifNotNull().transform(h -> {
			this.caregoryFullUpdateMapper.mapFullUpdate(category, h);
			return h;
		});
	}

	@WithSpan("CategoryService.partialUpdateCategory")
	@WithTransaction
	public Uni<Category> partialUpdateCategory(@SpanAttribute("arg.category") @NotNull Category category) {
		Log.infof("Partially updating category: %s", category);
		return this.categoryRepository.findById(category.getId()).onItem().ifNotNull().transform(h -> {
			this.categoryPartialUpdateMapper.mapPartialUpdate(category, h);
			return h;
		}).onItem().ifNotNull().transform(this::validatePartialUpdate);
	}

	@WithSpan("CategoryService.replaceAllCategories")
	@WithTransaction
	public Uni<Void> replaceAllCategories(@SpanAttribute("arg.categories") List<Category> categories) {
		Log.debug("Replacing all categories");
		return deleteAllCategories().replaceWith(this.categoryRepository.persist(categories));
	}

	/**
	 * Validates a {@link Category} for partial update according to annotation validation
	 * rules on the {@link Category} object.
	 * @param category The {@link Category}
	 * @return The same {@link Category} that was passed in, assuming it passes
	 * validation. The return is used as a convenience so the method can be called in a
	 * functional pipeline.
	 * @throws ConstraintViolationException If validation fails
	 */
	private Category validatePartialUpdate(Category category) {
		var violations = this.validator.validate(category);

		if ((violations != null) && !violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		return category;
	}

	@WithSpan("CategoryService.deleteAllCategories")
	@WithTransaction
	public Uni<Void> deleteAllCategories() {
		Log.debug("Deleting all categories");
		return this.categoryRepository.listAll()
			.onItem()
			.transformToMulti(list -> Multi.createFrom().iterable(list))
			.map(Category::getId)
			.onItem()
			.transformToUniAndMerge(this::deleteCategory)
			.collect()
			.asList()
			.replaceWithVoid();
	}

	@WithSpan("CategoryService.deleteCategory")
	@WithTransaction
	public Uni<Void> deleteCategory(@SpanAttribute("arg.id") UUID id) {
		Log.debugf("Deleting category by id = %s", id);
		return this.categoryRepository.deleteById(id).replaceWithVoid();
	}

}
