/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import ia.magazenn.category.Category;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
@TestMethodOrder(OrderAnnotation.class)
class CategoryResourceIT {

	private static final int DEFAULT_ORDER = 0;

	private static final String DEFAULT_NAME = "Super Chocolatine";

	private static final String UPDATED_NAME = DEFAULT_NAME + " (updated)";

	private static final String DEFAULT_DESCRIPTION = "Super Chocolatine chocolate in";

	private static final String UPDATED_DESCRIPTION = DEFAULT_DESCRIPTION + " (updated)";

	private static final int NB_CATEGORIES = 16;

	private static String categoryId;

	@BeforeAll
	static void beforeAll() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Test
	@Order(DEFAULT_ORDER)
	void helloEndpoint() {
		given().when()
			.accept(TEXT_PLAIN)
			.get("/api/categories/hello")
			.then()
			.statusCode(200)
			.body(is("Hello Category Resource"));
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotGetUnknownCategory() {
		get("/api/categories/{id}", UUID.randomUUID()).then().statusCode(NOT_FOUND.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldGetRandomCategoryFound() {
		get("/api/categories/random").then().statusCode(OK.getStatusCode()).contentType(JSON).body("$", notNullValue());
	}

	@Test
	@Order(DEFAULT_ORDER)
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
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotFullyUpdateInvalidItem() {
		var category = new Category();
		category.setId(UUID.fromString("560281a2-75ba-49cf-b2ee-3a7c4c6cd916"));
		category.setName(null);
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories/{id}", category.getId())
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotPartiallyUpdateInvalidItem() {
		var category = new Category();
		category.setId(UUID.fromString("560281a2-75ba-49cf-b2ee-3a7c4c6cd916"));
		category.setName("");
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.patch("/api/categories/{id}", category.getId())
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotAddNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotFullyUpdateNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body("")
			.put("/api/categories/{id}", 1L)
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotFullyUpdateNotFoundItem() {
		Category category = new Category();
		category.setId(UUID.randomUUID());
		category.setName(UPDATED_NAME);
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body(category)
			.put("/api/categories/{id}", UUID.randomUUID())
			.then()
			.statusCode(NOT_FOUND.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotPartiallyUpdateNullItem() {
		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body("")
			.patch("/api/categories/{id}", UUID.randomUUID())
			.then()
			.statusCode(BAD_REQUEST.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotPartiallyUpdateNotFoundItem() {
		Category category = new Category();

		given().when()
			.contentType(JSON)
			.accept(JSON)
			.body(category)
			.patch("/api/categories/{id}", UUID.randomUUID())
			.then()
			.statusCode(NOT_FOUND.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldNotGetAnyCategoriesThatDontMatchFilterCriteria() {
		given().when()
			.queryParam("name_filter", "iooi90904890358349 8890re9ierkjlk;sdf098w459idxflkjdfjoiio4ue")
			.get("/api/categories")
			.then()
			.statusCode(OK.getStatusCode())
			.body("size()", is(0));
	}

	@Test
	@Order(DEFAULT_ORDER)
	void shouldGetCategoriesThatMatchFilterCriteria() {
		var categories = given().when()
			.queryParam("name_filter", "Veh")
			.get("/api/categories")
			.then()
			.statusCode(OK.getStatusCode())
			.extract()
			.body()
			.jsonPath()
			.getList(".", Category.class);

		assertThat(categories).hasSize(1).extracting(Category::getName).containsExactly("Vehicles");
	}

	@Test
	@Order(DEFAULT_ORDER + 1)
	void shouldGetInitialItems() {
		get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.body("size()", is(NB_CATEGORIES));
	}

	@Test
	@Order(DEFAULT_ORDER + 2)
	void shouldAddAnItem() {
		Category category = new Category();
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		String location = given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(CREATED.getStatusCode())
			.extract()
			.header(HttpHeaders.LOCATION);

		assertThat(location).isNotBlank().contains("/api/categories");

		// Stores the id
		String[] segments = location.split("/");
		categoryId = segments[segments.length - 1];

		assertThat(categoryId).isNotNull();

		var returnedCategory = get("/api/categories/{id}", categoryId).then()
			.contentType(JSON)
			.statusCode(OK.getStatusCode())
			.extract()
			.as(Category.class);

		assertThat(returnedCategory).isNotNull()
			.usingRecursiveComparison()
			.ignoringFields("id")
			.isEqualTo(createDefaultCategory());

		verifyNumberOfCategories(NB_CATEGORIES + 1);
	}

	private static Category createDefaultCategory() {
		var category = new Category();
		category.setName(DEFAULT_NAME);
		category.setDescription(DEFAULT_DESCRIPTION);

		return category;
	}

	private static Category createUpdatedCategory() {
		var category = new Category();
		category.setName(UPDATED_NAME);
		category.setDescription(UPDATED_DESCRIPTION);

		return category;
	}

	private static void verifyNumberOfCategories(int expected) {
		get("/api/categories").then().statusCode(OK.getStatusCode()).contentType(JSON).body("size()", is(expected));
	}

	@Test
	@Order(DEFAULT_ORDER + 3)
	void shouldFullyUpdateAnItem() {
		Category category = new Category();
		category.setId(UUID.fromString(categoryId));
		category.setName(UPDATED_NAME);
		category.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories/{id}", category.getId())
			.then()
			.statusCode(NO_CONTENT.getStatusCode())
			.body(blankOrNullString());

		get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.body("size()", is(NB_CATEGORIES + 1));

		var updatedCategory = get("/api/categories/{id}", category.getId()).then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.as(Category.class);

		assertThat(updatedCategory).isNotNull().usingRecursiveComparison().isEqualTo(category);
	}

	@Test
	@Order(DEFAULT_ORDER + 4)
	void shouldPartiallyUpdateAnItem() {
		Category category = new Category();

		var expectedCategory = new Category();
		expectedCategory.setName(UPDATED_NAME);
		expectedCategory.setDescription(UPDATED_DESCRIPTION);
		expectedCategory.setId(UUID.fromString(categoryId));

		var patchedCategory = given().when()
			.body(category)
			.contentType(JSON)
			.accept(JSON)
			.patch("/api/categories/{id}", categoryId)
			.then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.as(Category.class);

		assertThat(patchedCategory).isNotNull().usingRecursiveComparison().isEqualTo(expectedCategory);

		get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.body("size()", is(NB_CATEGORIES + 1));
	}

	@Test
	@Order(DEFAULT_ORDER + 5)
	void shouldDeleteCategory() {
		delete("/api/categories/{id}", categoryId).then()
			.statusCode(NO_CONTENT.getStatusCode())
			.body(blankOrNullString());

		get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.body("size()", is(NB_CATEGORIES));
	}

	@Test
	@Order(DEFAULT_ORDER + 6)
	void shouldDeleteAllCategories() {
		delete("/api/categories/").then().statusCode(NO_CONTENT.getStatusCode()).body(blankOrNullString());

		get("/api/categories").then().statusCode(OK.getStatusCode()).body("$.size()", is(0));
	}

	@Test
	@Order(DEFAULT_ORDER + 7)
	void shouldGetRandomCategoryNotFound() {
		get("/api/categories/random").then().statusCode(NOT_FOUND.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER + 8)
	void shouldReplaceAllCategories() {
		var h1 = new Category();
		h1.setName(DEFAULT_NAME);
		h1.setDescription(DEFAULT_DESCRIPTION);

		var h2 = new Category();
		h2.setName(UPDATED_NAME);
		h2.setDescription(UPDATED_DESCRIPTION);

		given().when()
			.body(h1)
			.contentType(JSON)
			.accept(JSON)
			.post("/api/categories")
			.then()
			.statusCode(CREATED.getStatusCode())
			.header(HttpHeaders.LOCATION, containsString("/api/categories"));

		verifyNumberOfCategories(1);

		given().when()
			.body(List.of(h1, h2))
			.contentType(JSON)
			.accept(JSON)
			.put("/api/categories")
			.then()
			.statusCode(CREATED.getStatusCode())
			.header(HttpHeaders.LOCATION, endsWith("/api/categories"));

		var categories = get("/api/categories").then()
			.statusCode(OK.getStatusCode())
			.contentType(JSON)
			.extract()
			.body()
			.jsonPath()
			.getList(".", Category.class);

		assertThat(categories).hasSize(2)
			.usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
			.containsExactly(createDefaultCategory(), createUpdatedCategory());
	}

	@Test
	void shouldPingOpenAPI() {
		given().when().accept(JSON).get("/q/openapi").then().statusCode(OK.getStatusCode());
	}

}
