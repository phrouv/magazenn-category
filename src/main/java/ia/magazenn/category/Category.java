/*
 * Quarkus Magazenn Categories Microservice - The Magazenn Categories RESTful microservice
 * Copyright (c) 2024-2025 Magazenn. All Rights Reserved.
 *
 * This software including all the files whether in source code form or binary form is confidential and proprietary information of Magazenn.
 * The software shall be used only in accordance with the terms of the license agreement between you and Magazenn.
 */
package ia.magazenn.category;

import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * JPA entity class for a Category. Re-used in the API layer.
 */
@Entity
@Table(name = "CATEGORY", schema = "public")
@Data
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID", columnDefinition = "uuid", updatable = false, nullable = false)
	public UUID id;

	@NotNull
	@Size(min = 3, max = 50)
	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	@Size(min = 0, max = 300)
	@Column(name = "DESCRIPTION", length = 300)
	private String description;

}
