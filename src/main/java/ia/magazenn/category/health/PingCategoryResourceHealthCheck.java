/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import ia.magazenn.category.rest.CategoryResource;

/**
 * {@link HealthCheck} to ping the Category service
 */
@Liveness
public class PingCategoryResourceHealthCheck implements HealthCheck {

	private final CategoryResource categoryResource;

	public PingCategoryResourceHealthCheck(CategoryResource categoryResource) {
		this.categoryResource = categoryResource;
	}

	@Override
	public HealthCheckResponse call() {
		var response = this.categoryResource.hello();

		return HealthCheckResponse.named("Ping Category REST Endpoint").withData("Response", response).up().build();
	}

}
