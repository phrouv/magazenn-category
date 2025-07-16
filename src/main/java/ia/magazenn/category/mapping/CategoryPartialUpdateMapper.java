/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import ia.magazenn.category.Category;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

/**
 * Mapper to map <code><strong>non-null</strong></code> fields on an input
 * {@link Category} onto a target {@link Category}.
 */
@Mapper(componentModel = ComponentModel.JAKARTA_CDI, nullValuePropertyMappingStrategy = IGNORE)
public interface CategoryPartialUpdateMapper {

	/**
	 * Maps all <code><strong>non-null</strong></code> fields from {@code input} onto
	 * {@code target}.
	 * @param input The input {@link Category}
	 * @param target The target {@link Category}
	 */
	void mapPartialUpdate(Category input, @MappingTarget Category target);

}
