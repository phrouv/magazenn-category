/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.HttpHeaders;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import ia.magazenn.category.Category;
import ia.magazenn.category.service.CategoryService;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CategoryResourceTests {

	private static final String DEFAULT_NAME = "Super Chocolatine";

	private static final String UPDATED_NAME = DEFAULT_NAME + " (updated)";

	private static final String DEFAULT_DESCRIPTION = "Super Chocolatine chocolate in";

	private static final String UPDATED_DESCRIPTION = DEFAULT_DESCRIPTION + " (updated)";

	private static final UUID DEFAULT_ID = UUID.randomUUID();

	@InjectMock
	CategoryService categoryService;

	@BeforeAll
	static void beforeAll() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Test
	void helloEndpoint() {
		get("/api/categories/hello").then().statusCode(200).body(is("Hello Category Resource"));

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldNotGetUnknownCategory() {
		when(this.categoryService.findCategoryById(DEFAULT_ID)).thenReturn(Uni.createFrom().nullItem());

		get("/api/categories/{id}", DEFAULT_ID).then().statusCode(NOT_FOUND.getStatusCode());

		verify(this.categoryService).findCategoryById(DEFAULT_ID);
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetRandomCategoryNotFound() {
		when(this.categoryService.findRandomCategory()).thenReturn(Uni.createFrom().nullItem());

		get("/api/categories/random").then().statusCode(NOT_FOUND.getStatusCode());

		verify(this.categoryService).findRandomCategory();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetRandomCategoryFound() {
		when(this.categoryService.findRandomCategory()).thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var category = get("/api/categories/random").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.as(Category.class);

		assertThat(category).isNotNull()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryService).findRandomCategory();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldNotAddInvalidItem() {
		var category = new Category();
		category.setName(null);
		category.setDescription(DEFAULT_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldNotAddNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldNotFullyUpdateNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body("")
			.put("/api/categories/{id}", DEFAULT_ID)
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldNotFullyUpdateInvalidItem() {
		var category = createFullyUpdatedCategory();
		category.setName(null);
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories/{id}", category.getId())
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldNotPartiallyUpdateInvalidItem() {
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId().equals(DEFAULT_ID) && (h.getName() == null)
				&& h.getDescription().equals(UPDATED_DESCRIPTION));

		when(this.categoryService.partialUpdateCategory(argThat(categoryMatcher)))
			.thenReturn(Uni.createFrom().failure(new ConstraintViolationException(Set.of())));

		var category = createPartiallyUpdatedCategory();
		category.setName(null);
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.patch("/api/categories/{id}", DEFAULT_ID)
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verify(this.categoryService).partialUpdateCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldNotPartiallyUpdateNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body("")
			.patch("/api/categories/{id}", DEFAULT_ID)
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());

		verifyNoInteractions(this.categoryService);
	}

	@Test
	void shouldGetItems() {
		when(this.categoryService.findAllCategories())
			.thenReturn(Uni.createFrom().item(List.of(createDefaultCategory())));

		var categories = get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.body()
			.jsonPath()
			.getList(".", Category.class);

		assertThat(categories).singleElement()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryService).findAllCategories();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetEmptyItems() {
		when(this.categoryService.findAllCategories()).thenReturn(Uni.createFrom().item(List.of()));

		get("/api/categories").then().statusCode(OK.getStatusCode()).body("$.size()", is(0));

		verify(this.categoryService).findAllCategories();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetItemsWithNameFilter() {
		when(this.categoryService.findAllCategoriesHavingName("name"))
			.thenReturn(Uni.createFrom().item(List.of(createDefaultCategory())));

		var categories = given().when()
			.queryParam("name_filter", "name")
			.get("/api/categories")
			.then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.body()
			.jsonPath()
			.getList(".", Category.class);

		assertThat(categories).singleElement()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryService).findAllCategoriesHavingName("name");
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetEmptyItemsWithNameFilter() {
		when(this.categoryService.findAllCategoriesHavingName("name")).thenReturn(Uni.createFrom().item(List.of()));

		given().when()
			.queryParam("name_filter", "name")
			.get("/api/categories")
			.then()
			.statusCode(OK.getStatusCode())
			.body("$.size()", is(0));

		verify(this.categoryService).findAllCategoriesHavingName("name");
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldGetNullItems() {
		when(this.categoryService.findAllCategories()).thenReturn(Uni.createFrom().nullItem());

		get("/api/categories").then().statusCode(OK.getStatusCode()).body("$.size()", is(0));

		verify(this.categoryService).findAllCategories();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldAddAnItem() {
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId() == null) && h.getName().equals(DEFAULT_NAME)
				&& h.getDescription().equals(DEFAULT_DESCRIPTION);

		when(this.categoryService.persistCategory(argThat(categoryMatcher)))
			.thenReturn(Uni.createFrom().item(createDefaultCategory()));

		var category = new Category();
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(CREATED.getStatusCode())
			.header(HttpHeaders.LOCATION, containsString("/api/categories/" + DEFAULT_ID));

		verify(this.categoryService).persistCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldNotFullyUpdateNotFoundItem() {
		var category = createFullyUpdatedCategory();
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId().equals(DEFAULT_ID)
				&& h.getName().equals(UPDATED_NAME) && h.getDescription().equals(UPDATED_DESCRIPTION));

		when(this.categoryService.replaceCategory(argThat(categoryMatcher))).thenReturn(Uni.createFrom().nullItem());

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories/{id}", category.getId())
			.then()
			.statusCode(NOT_FOUND.getStatusCode())
			.body(blankOrNullString());

		verify(this.categoryService).replaceCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldFullyUpdateAnItem() {
		var category = createFullyUpdatedCategory();
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId().equals(DEFAULT_ID)
				&& h.getName().equals(UPDATED_NAME) && h.getDescription().equals(UPDATED_DESCRIPTION));

		when(this.categoryService.replaceCategory(argThat(categoryMatcher)))
			.thenReturn(Uni.createFrom().item(category));

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories/{id}", category.getId())
			.then()
			.statusCode(NO_CONTENT.getStatusCode())
			.body(blankOrNullString());

		verify(this.categoryService).replaceCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldNotPartiallyUpdateNotFoundItem() {
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId().equals(DEFAULT_ID) && (h.getName() == null)
				&& (h.getDescription() == null));

		var partialCategory = new Category();

		when(this.categoryService.partialUpdateCategory(argThat(categoryMatcher)))
			.thenReturn(Uni.createFrom().nullItem());

		given().when()
			.body(partialCategory)
			.contentType(JSON)
			.accept(JSON)
			.patch("/api/categories/{id}", DEFAULT_ID)
			.then()
			.statusCode(NOT_FOUND.getStatusCode())
			.body(blankOrNullString());

		verify(this.categoryService).partialUpdateCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldPartiallyUpdateAnItem() {
		ArgumentMatcher<Category> categoryMatcher = h -> (h.getId().equals(DEFAULT_ID) && (h.getName() == null)
				&& (h.getDescription() == null));

		var partialCategory = new Category();

		when(this.categoryService.partialUpdateCategory(argThat(categoryMatcher)))
			.thenReturn(Uni.createFrom().item(createPartiallyUpdatedCategory()));

		var category = given().when()
			.body(partialCategory)
			.contentType(JSON)
			.accept(JSON)
			.patch("/api/categories/{id}", DEFAULT_ID)
			.then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.as(Category.class);

		assertThat(category).isNotNull()
			.extracting(Category::getId, Category::getName, Category::getDescription)
			.containsExactly(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);

		verify(this.categoryService).partialUpdateCategory(argThat(categoryMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldDeleteCategory() {
		when(this.categoryService.deleteCategory(DEFAULT_ID)).thenReturn(Uni.createFrom().voidItem());

		delete("/api/categories/{id}", DEFAULT_ID).then()
			.statusCode(NO_CONTENT.getStatusCode())
			.body(blankOrNullString());

		verify(this.categoryService).deleteCategory(DEFAULT_ID);
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldReplaceAllCategories() {
		var categories = List.of(createDefaultCategory(), createFullyUpdatedCategory());
		categories.forEach(h -> h.setId(null));

		ArgumentMatcher<List<Category>> categoriesMatcher = h -> (h.size() == 2) && (h.get(0).getId() == null)
				&& h.get(0).getName().equals(DEFAULT_NAME) && h.get(0).getDescription().equals(DEFAULT_DESCRIPTION)
				&& (h.get(1).getId() == null) && h.get(1).getName().equals(UPDATED_NAME)
				&& h.get(1).getDescription().equals(UPDATED_DESCRIPTION);

		when(this.categoryService.replaceAllCategories(argThat(categoriesMatcher)))
			.thenReturn(Uni.createFrom().voidItem());

		given().when()
			.body(categories)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories")
			.then()
			.statusCode(CREATED.getStatusCode())
			.header(HttpHeaders.LOCATION, Matchers.endsWith("/api/categories"));

		verify(this.categoryService).replaceAllCategories(argThat(categoriesMatcher));
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldDeleteAllCategories() {
		when(this.categoryService.deleteAllCategories()).thenReturn(Uni.createFrom().voidItem());

		delete("/api/categories").then().statusCode(NO_CONTENT.getStatusCode()).body(blankOrNullString());

		verify(this.categoryService).deleteAllCategories();
		verifyNoMoreInteractions(this.categoryService);
	}

	@Test
	void shouldPingOpenAPI() {
		get("/q/openapi").then().statusCode(OK.getStatusCode());
	}

	@Test
	void shouldPingHealthCheck() {
		var expected = Map.of("name", "Ping Category REST Endpoint", "status", "UP", "data",
				Map.of("Response", "Hello Category Resource"));

		var health = get("/q/health/live").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.as(Map.class);

		assertThat(health).isNotNull().containsKey("checks");

		var pingElement = ((List<Map<String, Object>>) health.get("checks")).stream()
			.filter(map -> "Ping Category REST Endpoint".equals(map.get("name")))
			.findFirst();

		assertThat(pingElement).isPresent();

		assertThat(pingElement.get()).containsAllEntriesOf(expected);
	}

	private static Category createDefaultCategory() {
		var category = new Category();
		category.setId(DEFAULT_ID);
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		return category;
	}

	public static Category createFullyUpdatedCategory() {
		var category = createDefaultCategory();
		category.setName(UPDATED_NAME);
		category.setDescription(UPDATED_DESCRIPTION);

		return category;
	}

	public static Category createPartiallyUpdatedCategory() {
		var category = createDefaultCategory();

		return category;
	}

}
