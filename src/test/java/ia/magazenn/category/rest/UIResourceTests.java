/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

import java.net.URL;
import java.util.List;

import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;

import ia.magazenn.category.Category;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@WithPlaywright(recordVideoDir = "target/playwright", slowMo = 500)
class UIResourceTests {

	private static final int NB_CATEGORIES = 16;

	private static final Category VEHICLES = getVehicles();

	@InjectPlaywright
	BrowserContext browserContext;

	@TestHTTPResource("/")
	URL index;

	@Test
	void indexLoads() {
		var page = loadPage();

		assertThat(page.title()).isNotNull().isEqualTo("Categories List");
	}

	@Test
	void correctTable() {
		var table = getAndVerifyTable(NB_CATEGORIES);

		assertThat(table).isNotNull();

		var tableColumns = table.getByRole(AriaRole.COLUMNHEADER).all();
		assertThat(tableColumns).isNotNull()
			.hasSize(3)
			.extracting(Locator::textContent)
			.containsExactly("ID", "Name", "Description");
	}

	@Test
	void tableFilters() {
		var page = loadPage();
		getAndVerifyTable(page, NB_CATEGORIES);

		// Fill in the filter
		page.getByPlaceholder("Filter by name").fill(VEHICLES.getName());

		// Click the filter button
		page.getByText("Filter Categories").click();

		// Get and verify the correct thing shows after the filter
		var table = getAndVerifyTable(page, 1);
		var tableRows = table.getByRole(AriaRole.ROW).all();

		assertThat(tableRows).isNotNull().hasSize(1);

		var tableCells = tableRows.get(0).getByRole(AriaRole.CELL).all();
		assertThat(tableCells).isNotNull().hasSize(3);

		// For the ID field, just test that it's there
		assertThat(tableCells.get(0).textContent()).isNotNull().isNotEmpty();

		// For the Name and description fields, assert the values
		var textValues = List.of(tableCells.get(1).textContent(), tableCells.get(2).textContent());

		assertThat(textValues).satisfies(name -> assertThat(name).isEqualTo(VEHICLES.getName()), atIndex(0))
			.satisfies(description -> assertThat(description).isEqualTo(VEHICLES.getDescription()), atIndex(1));
	}

	private Page loadPage() {
		var page = this.browserContext.newPage();
		var response = page.navigate(this.index.toString());

		assertThat(response).isNotNull().extracting(Response::status).isEqualTo(Status.OK.getStatusCode());

		return page;
	}

	private Locator getAndVerifyTable(Page page, int expectedNumRows) {
		var table = page.getByRole(AriaRole.GRID);

		assertThat(table).isNotNull();

		var tableRowCount = table.getByRole(AriaRole.ROW).count();
		assertThat(tableRowCount).isEqualTo(expectedNumRows);

		return table;
	}

	private Locator getAndVerifyTable(int expectedNumRows) {
		return getAndVerifyTable(loadPage(), expectedNumRows);
	}

	private static Category getVehicles() {
		var vehicles = new Category();
		vehicles.setName("Vehicles");
		vehicles.setDescription("Vehicles");

		return vehicles;
	}

}
