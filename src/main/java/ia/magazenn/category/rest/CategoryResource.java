/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ia.magazenn.category.Category;
import ia.magazenn.category.service.CategoryService;

import static jakarta.ws.rs.core.MediaType.*;

/**
 * JAX-RS API endpoints with <code>/api/categories</code> as the base URI for all
 * endpoints
 */
@Path("/api/categories")
@Tag(name = "categories")
@Produces(APPLICATION_JSON)
public class CategoryResource {

	private final CategoryService categoryService;

	public CategoryResource(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GET
	@Path("/random")
	@Operation(summary = "Returns a random category")
	@APIResponse(responseCode = "200", description = "Gets a random category",
			content = @Content(mediaType = APPLICATION_JSON,
					schema = @Schema(implementation = Category.class, required = true),
					examples = @ExampleObject(name = "category", value = Examples.VALID_EXAMPLE_CATEGORY)))
	@APIResponse(responseCode = "404", description = "No category found")
	public Uni<Response> getRandomCategory() {
		return this.categoryService.findRandomCategory().onItem().ifNotNull().transform(h -> {
			Log.debugf("Found random category: %s", h);
			return Response.ok(h).build();
		}).replaceIfNullWith(() -> {
			Log.debug("No random category found");
			return Response.status(Status.NOT_FOUND).build();
		});
	}

	@GET
	@Operation(summary = "Returns all the categories from the database")
	@APIResponse(responseCode = "200", description = "Gets all categories",
			content = @Content(mediaType = APPLICATION_JSON,
					schema = @Schema(implementation = Category.class, type = SchemaType.ARRAY),
					examples = @ExampleObject(name = "categories", value = Examples.VALID_EXAMPLE_CATEGORY_LIST)))
	public Uni<List<Category>> getAllCategories(@Parameter(name = "name_filter",
			description = "An optional filter parameter to filter results by name") @QueryParam("name_filter") Optional<String> nameFilter) {
		return nameFilter.map(this.categoryService::findAllCategoriesHavingName)
			.orElseGet(() -> this.categoryService.findAllCategories().replaceIfNullWith(List::of))
			.invoke(categories -> Log.debugf("Total number of categories: %d", categories.size()));
	}

	@GET
	@Path("/{id}")
	@Operation(summary = "Returns a category for a given identifier")
	@APIResponse(responseCode = "200", description = "Gets a category for a given id",
			content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Category.class),
					examples = @ExampleObject(name = "category", value = Examples.VALID_EXAMPLE_CATEGORY)))
	@APIResponse(responseCode = "404", description = "The category is not found for a given identifier")
	public Uni<Response> getCategory(@Parameter(name = "id", required = true) @PathParam("id") UUID id) {
		return this.categoryService.findCategoryById(id).onItem().ifNotNull().transform(h -> {
			Log.debugf("Found category: %s", h);
			return Response.ok(h).build();
		}).replaceIfNullWith(() -> {
			Log.debugf("No category found with id %d", id);
			return Response.status(Status.NOT_FOUND).build();
		});
	}

	@POST
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Creates a valid category")
	@APIResponse(responseCode = "201", description = "The URI of the created category",
			headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class)))
	@APIResponse(responseCode = "400", description = "Invalid category passed in (or no request body found)")
	public Uni<Response> createCategory(
			@RequestBody(name = "category", required = true, content = @Content(mediaType = APPLICATION_JSON,
					schema = @Schema(implementation = Category.class),
					examples = @ExampleObject(name = "valid_category",
							value = Examples.VALID_EXAMPLE_CATEGORY_TO_CREATE))) @Valid @NotNull Category category,
			@Context UriInfo uriInfo) {
		return this.categoryService.persistCategory(category).map(h -> {
			var uri = uriInfo.getAbsolutePathBuilder().path(h.getId().toString()).build();
			Log.debugf("New Category created with URI %s", uri.toString());
			return Response.created(uri).build();
		});
	}

	@PUT
	@Path("/{id}")
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Completely updates/replaces an exiting category by replacing it with the passed-in category")
	@APIResponse(responseCode = "204", description = "Replaced the category")
	@APIResponse(responseCode = "400", description = "Invalid category passed in (or no request body found)")
	@APIResponse(responseCode = "404", description = "No category found")
	public Uni<Response> fullyUpdateCategory(@Parameter(name = "id", required = true) @PathParam("id") String id,
			@RequestBody(name = "category", required = true,
					content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Category.class),
							examples = @ExampleObject(name = "valid_category",
									value = Examples.VALID_EXAMPLE_CATEGORY))) @Valid @NotNull Category category) {
		if (category.getId() == null) {
			try {
				category.setId(UUID.fromString(id));
			}
			catch (IllegalArgumentException exc) {
				Log.debugf("Invalid id %s", id);
				return Uni.createFrom().failure(new IllegalArgumentException("Invalid UUID string: " + id));
			}
		}

		return this.categoryService.replaceCategory(category).onItem().ifNotNull().transform(h -> {
			Log.debugf("Category replaced with new values %s", h);
			return Response.noContent().build();
		}).replaceIfNullWith(() -> {
			Log.debugf("No category found with id %d", category.getId());
			return Response.status(Status.NOT_FOUND).build();
		});
	}

	@PUT
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Completely replace all categories with the passed-in categories")
	@APIResponse(responseCode = "201", description = "The URI to retrieve all the created categories",
			headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class)))
	@APIResponse(responseCode = "400", description = "Invalid categories passed in (or no request body found)")
	public Uni<Response> replaceAllCategories(
			@RequestBody(name = "valid_categories", required = true,
					content = @Content(mediaType = APPLICATION_JSON,
							schema = @Schema(implementation = Category.class, type = SchemaType.ARRAY),
							examples = @ExampleObject(name = "categories",
									value = Examples.VALID_EXAMPLE_CATEGORY_LIST))) @NotNull List<Category> categories,
			@Context UriInfo uriInfo) {
		return this.categoryService.replaceAllCategories(categories).map(h -> {
			var uri = uriInfo.getAbsolutePathBuilder().build();
			Log.debugf("New categories created with URI %s", uri.toString());
			return Response.created(uri).build();
		});
	}

	@PATCH
	@Path("/{id}")
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Partially updates an exiting category")
	@APIResponse(responseCode = "200", description = "Updated the category",
			content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Category.class),
					examples = @ExampleObject(name = "category", value = Examples.VALID_EXAMPLE_CATEGORY)))
	@APIResponse(responseCode = "400", description = "Null category passed in (or no request body found)")
	@APIResponse(responseCode = "404", description = "No category found")
	public Uni<Response> partiallyUpdateCategory(@Parameter(name = "id", required = true) @PathParam("id") String id,
			@RequestBody(name = "valid_category", required = true,
					content = @Content(schema = @Schema(implementation = Category.class),
							examples = @ExampleObject(name = "valid_category",
									value = Examples.VALID_EXAMPLE_CATEGORY))) @NotNull Category category) {
		if (category.getId() == null) {
			try {
				category.setId(UUID.fromString(id));
			}
			catch (IllegalArgumentException exc) {
				Log.debugf("Invalid id %s", id);
				return Uni.createFrom().failure(new IllegalArgumentException("Invalid UUID string: " + id));
			}
		}

		return this.categoryService.partialUpdateCategory(category).onItem().ifNotNull().transform(h -> {
			Log.debugf("Category updated with new values %s", h);
			return Response.ok(h).build();
		}).replaceIfNullWith(() -> {
			Log.debugf("No category found with id %d", category.getId());
			return Response.status(Status.NOT_FOUND).build();
		})
			.onFailure(ConstraintViolationException.class)
			.transform(cve -> new ResteasyReactiveViolationException(
					((ConstraintViolationException) cve).getConstraintViolations()));
	}

	@DELETE
	@Operation(summary = "Delete all categories")
	@APIResponse(responseCode = "204", description = "Deletes all categories")
	public Uni<Void> deleteAllCategories() {
		return this.categoryService.deleteAllCategories().invoke(() -> Log.debug("Deleted all categories"));
	}

	@DELETE
	@Path("/{id}")
	@Operation(summary = "Deletes an exiting category")
	@APIResponse(responseCode = "204", description = "Deletes a category")
	@APIResponse(responseCode = "400", description = "Invalid category id passed in")
	public Uni<Void> deleteCategory(@Parameter(name = "id", required = true) @PathParam("id") String id) {
		return this.categoryService.deleteCategory(UUID.fromString(id))
			.invoke(() -> Log.debugf("Category deleted with %s", id));
	}

	@GET
	@Path("/hello")
	@Produces(TEXT_PLAIN)
	@Tag(name = "hello")
	@Operation(summary = "Ping hello")
	@APIResponse(responseCode = "200", description = "Ping hello",
			content = @Content(schema = @Schema(implementation = String.class),
					examples = @ExampleObject(name = "hello_success", value = "Hello Category Resource")))
	@NonBlocking
	public String hello() {
		Log.debug("Hello Category Resource");
		return "Hello Category Resource";
	}

}
