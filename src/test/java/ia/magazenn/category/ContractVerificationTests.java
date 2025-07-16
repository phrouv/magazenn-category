/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import ia.magazenn.category.repository.CategoryRepository;

import static org.mockito.Mockito.doReturn;

@QuarkusTest
@Provider("rest-categories")
@PactFolder("pacts")
// You could comment out the @PactFolder annotation
// if you'd like to use a Pact broker. You'd also un-comment the following 2 annotations
// @PactBroker(url = "https://quarkus-magazenn.pactflow.io")
// @EnabledIfSystemProperty(named = "pactbroker.auth.token", matches = ".+",
// disabledReason = "pactbroker.auth.token system property not set")
public class ContractVerificationTests {

	private static final String NO_RANDOM_CATEGORY_FOUND_STATE = "No random category found";

	@ConfigProperty(name = "quarkus.http.test-port")
	int quarkusPort;

	@InjectSpy
	CategoryRepository categoryRepository;

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void pactVerificationTestTemplate(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@BeforeEach
	void beforeEach(PactVerificationContext context) {
		context.setTarget(new HttpTestTarget("localhost", this.quarkusPort));

		// Have to do this here because the CDI context doesn't seem to be available
		// in the @State method below
		var isNoRandomCategoryFoundState = Optional.ofNullable(context.getInteraction().getProviderStates())
			.orElseGet(List::of)
			.stream()
			.filter(state -> NO_RANDOM_CATEGORY_FOUND_STATE.equals(state.getName()))
			.count() > 0;

		if (isNoRandomCategoryFoundState) {
			doReturn(Uni.createFrom().nullItem()).when(this.categoryRepository).findRandom();
		}
	}

	@PactBrokerConsumerVersionSelectors
	public static SelectorBuilder consumerVersionSelectors() {
		return new SelectorBuilder().branch(System.getProperty("pactbroker.consumer.branch", "main"));
	}

	@State(NO_RANDOM_CATEGORY_FOUND_STATE)
	public void clearData() {
		// Already handled in beforeEach
	}

}
