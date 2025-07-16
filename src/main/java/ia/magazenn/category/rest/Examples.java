/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.rest;

final class Examples {

	private Examples() {

	}

	static final String VALID_EXAMPLE_CATEGORY = """
			{
				"id": "560281a2-75ba-49cf-b2ee-3a7c4c6cd916",
				"name": "Vehicles",
				"description": "Vehicles"
			}
			""";

	static final String VALID_EXAMPLE_CATEGORY_TO_CREATE = """
			  {
				"name": "Electronics",
				"description": "Electronics"
			}
			""";

	static final String VALID_EXAMPLE_CATEGORY_LIST = "[" + VALID_EXAMPLE_CATEGORY + "]";

}
