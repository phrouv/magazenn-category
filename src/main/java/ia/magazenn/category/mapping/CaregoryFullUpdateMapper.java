/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import ia.magazenn.category.Category;

/**
 * Mapper to map all fields on an input {@link Category} onto a target {@link Category}.
 */
@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface CaregoryFullUpdateMapper {

	/**
	 * Maps all fields except <code>id</code> from {@code input} onto {@code target}.
	 * @param input The input {@link Category}
	 * @param target The target {@link Category}
	 */
	@Mapping(target = "id", ignore = true)
	void mapFullUpdate(Category input, @MappingTarget Category target);

}
