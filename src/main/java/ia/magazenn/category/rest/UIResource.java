/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import ia.magazenn.category.Category;
import ia.magazenn.category.service.CategoryService;

@Path("/")
public class UIResource {

	private final CategoryService categoryService;

	public UIResource(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@CheckedTemplate
	static class Templates {

		static native TemplateInstance index(List<Category> categories);

	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Uni<String> get(@QueryParam("name_filter") Optional<String> nameFilter) {
		return nameFilter.map(this.categoryService::findAllCategoriesHavingName)
			.orElseGet(this.categoryService::findAllCategories)
			.map(Templates::index)
			.flatMap(TemplateInstance::createUni);
	}

}
