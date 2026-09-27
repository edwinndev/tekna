# Tekna API

Architecture guide and development standard for **Tekna API**.

## Project Overview

- **Base Package**: `com.tekna.api`
- **Java Version**: 21
- **Framework**: Spring Boot 3.x / 4.x
- **Database Migration**: Flyway (MySQL)
- **API Documentation**: OpenAPI / Swagger (`springdoc-openapi`)
- **Mapping**: MapStruct + Lombok
- **Validation**: Jakarta Validation (`jakarta.validation.*`)
- **Date & Time Standard**: `java.time.Instant` in Java, `TIMESTAMP(3)` in Database.

---

## Architectural Guidelines & Best Practices

### 1. Package Structure
```text
com.tekna.api
├── common
│   ├── entity
│   │   └── BaseEntity.java
│   ├── exception
│   │   ├── ApplicationErrorResponse.java
│   │   ├── ApplicationException.java
│   │   ├── BadRequestException.java
│   │   ├── InternalServerException.java
│   │   └── NotFoundException.java
│   └── pagination
│       ├── PageOutput.java
│       └── SearchInput.java
├── config
│   ├── GlobalExceptionHandler.java
│   ├── OpenApiConfig.java
│   └── JpaConfig.java
└── product
    ├── controller
    │   └── ProductController.java
    ├── dto
    │   ├── CreateProductInput.java
    │   ├── UpdateProductInput.java
    │   ├── ProductFilterInput.java
    │   └── ProductOutput.java
    ├── mapper
    │   └── ProductMapper.java
    ├── persistence
    │   ├── entity
    │   │   └── Product.java
    │   ├── repository
    │   │   └── ProductRepository.java
    │   └── specification
    │       └── ProductSpecification.java
    └── service
        ├── ProductService.java
        └── impl
            └── ProductServiceImpl.java
```

---

### 2. Persistence Layer (`persistence/entities` & `persistence/repository`)

#### Naming Conventions
- **Java Entities**: Singular and English (e.g., `Product`, `User`, `Category`).
- **Database Tables**: Plural, lowercase, snake_case in English (e.g., `products`, `users`, `categories`).

#### Primary Key & Audit Details
- Primary Key is always `UUID` (`java.util.UUID`).
- All temporal fields must strictly use `java.time.Instant` in Java and `TIMESTAMP(3)` in MySQL.
- Every table and entity must include 6 audit fields:
  1. `created_at` (`Instant` / `TIMESTAMP(3) NOT NULL`)
  2. `created_by` (`UUID` or `String`)
  3. `updated_at` (`Instant` / `TIMESTAMP(3) NULL`)
  4. `updated_by` (`UUID` or `String`)
  5. `deleted_at` (`Instant` / `TIMESTAMP(3) NULL`)
  6. `deleted_by` (`UUID` or `String`, nullable)

#### Soft Delete
- Physical deletes are **strictly forbidden**.
- Soft delete must be handled by setting `deleted_at` (and `deleted_by`).
- Every entity must be annotated with `@SQLRestriction("deleted_at is null")` to filter active records by default.

#### Base Entity Definition
```java
package com.tekna.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP(3)")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP(3)")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;
}
```

#### Entity Example
```java
package com.tekna.api.product.persistence.entity;

import com.tekna.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at is null")
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;
}
```

#### Repository Layer
- Repositories must extend `JpaRepository<Entity, UUID>` and `JpaSpecificationExecutor<Entity>`.
- Annotated with `@Repository`.
```java
package com.tekna.api.product.persistence.repository;

import com.tekna.api.product.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
}
```

---

### 3. Controller Layer & Request/Response Contracts

#### Rules & Naming
- **Route Prefix Rule**: **Never** prepend `/api` to controller routes. Always map directly to versioned resources: e.g., `@RequestMapping("/v1/products")` (never `@RequestMapping("/api/v1/products")`).
- **OpenAPI / Swagger Documentation Rules**:
  - Every controller **must** be annotated with `@Tag(name = "...", description = "...")`.
  - Every endpoint method **must** include `@Operation` and strictly **exactly 2** `@ApiResponse` annotations (neither 1 nor more than 2, typically the success response and the primary error response).
- Controllers receive **Inputs** and return **Outputs** (e.g., `CreateProductInput`, `UpdateProductInput`, `ProductOutput`).
- **Exception Delegation Rule**: Controllers **must never** throw exceptions or contain try-catch logic. All business validations and error conditions must be handled in the Service layer, which throws appropriate domain exceptions from `com.tekna.api.common.exception` (`NotFoundException`, `BadRequestException`, `InternalServerException`).
- Do not prepend `Create` or `Update` to the Output class; responses for creating and updating share the same contract (`ProductOutput`).
- Pass all payload values within the request body; avoid fractioned request parameters unless querying by ID via path variable (`GET /{id}`).
- All input objects must use `jakarta.validation` annotations (e.g., `@NotNull`, `@NotBlank`, `@Min`).
- Validation is triggered with `@Valid` on controller methods.

#### Request Inputs Structure
```java
package com.tekna.api.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductInput {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
}
```

```java
package com.tekna.api.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateProductInput extends CreateProductInput {

    @NotNull(message = "ID is required for update")
    private UUID id;
}
```

#### Response Output Structure
```java
package com.tekna.api.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ProductOutput {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Instant createdAt;
    private Instant updatedAt;
}
```

---

### 4. Pagination and Search Filtering

#### Filtering Rule
- `findAll()` is **strictly prohibited**.
- Use `POST /filter` calling `search(@Valid @RequestBody SearchInput<ProductFilterInput> input)`.
- Advanced filters are built dynamically using **JPA Specifications**.

#### Pagination Wrapper Models
```java
package com.tekna.api.common.pagination;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchInput<T> {

    @Min(value = 0, message = "Page number must be 0 or greater")
    private int pageNumber = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int pageSize = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    @Valid
    private T filter;
}
```

```java
package com.tekna.api.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageOutput<T> {
    private List<T> items;
    private int count;
    private long total;
    private int pageSize;
    private int pageNumber;
}
```

#### Controller Example
```java
package com.tekna.api.product.controller;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.product.dto.CreateProductInput;
import com.tekna.api.product.dto.ProductFilterInput;
import com.tekna.api.product.dto.ProductOutput;
import com.tekna.api.product.dto.UpdateProductInput;
import com.tekna.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ProductOutput> create(@Valid @RequestBody CreateProductInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(input));
    }

    @PutMapping
    @Operation(summary = "Update an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductOutput> update(@Valid @RequestBody UpdateProductInput input) {
        return ResponseEntity.ok(productService.update(input));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductOutput> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(summary = "Search products with pagination and dynamic filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search filter parameters")
    })
    public ResponseEntity<PageOutput<ProductOutput>> search(@Valid @RequestBody SearchInput<ProductFilterInput> input) {
        return ResponseEntity.ok(productService.search(input));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 5. Service & Mapper Layer

#### Service Interface & Implementation
- Use interfaces and implementations (e.g., `ProductService` and `ProductServiceImpl`).
- **Dependency Injection**: Strictly forbid `@Autowired` on field injection. Always use Lombok's `@RequiredArgsConstructor` with `private final` fields.
- **Transaction Management**: 
  - Annotate service classes with `@Transactional(readOnly = true)` for read optimization.
  - Annotate mutation methods (`create`, `update`, `delete`) with `@Transactional`.
- **Exception Handling**: Services are exclusively responsible for business logic validations and error handling, throwing specific exceptions from `com.tekna.api.common.exception` (e.g., `NotFoundException`, `BadRequestException`, `InternalServerException`).
- **Logging**: Strictly forbid `System.out.println` and `e.printStackTrace()`. Use `@Slf4j` from Lombok for structured logging.
- MapStruct is injected into the implementation for entity-to-DTO and DTO-to-entity mapping.

```java
package com.tekna.api.product.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.product.dto.CreateProductInput;
import com.tekna.api.product.dto.ProductFilterInput;
import com.tekna.api.product.dto.ProductOutput;
import com.tekna.api.product.dto.UpdateProductInput;

import java.util.UUID;

public interface ProductService {
    ProductOutput create(CreateProductInput input);
    ProductOutput update(UpdateProductInput input);
    ProductOutput getById(UUID id);
    PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input);
    void delete(UUID id);
}
```

```java
package com.tekna.api.product.service.impl;

import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.product.dto.CreateProductInput;
import com.tekna.api.product.dto.ProductFilterInput;
import com.tekna.api.product.dto.ProductOutput;
import com.tekna.api.product.dto.UpdateProductInput;
import com.tekna.api.product.mapper.ProductMapper;
import com.tekna.api.product.persistence.entity.Product;
import com.tekna.api.product.persistence.repository.ProductRepository;
import com.tekna.api.product.persistence.specification.ProductSpecification;
import com.tekna.api.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductOutput create(CreateProductInput input) {
        log.info("Creating new product with name: {}", input.getName());
        Product product = productMapper.toEntity(input);
        product.setCreatedBy("system");
        Product savedProduct = productRepository.save(product);
        return productMapper.toOutput(savedProduct);
    }

    @Override
    @Transactional
    public ProductOutput update(UpdateProductInput input) {
        log.info("Updating product with ID: {}", input.getId());
        Product product = productRepository.findById(input.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + input.getId()));
        productMapper.updateEntityFromInput(input, product);
        product.setUpdatedBy("system");
        Product updatedProduct = productRepository.save(product);
        return productMapper.toOutput(updatedProduct);
    }

    @Override
    public ProductOutput getById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    @Override
    public PageOutput<ProductOutput> search(SearchInput<ProductFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<Product> spec = ProductSpecification.withFilter(input.getFilter());

        Page<Product> page = productRepository.findAll(spec, pageable);
        return new PageOutput<>(
                productMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Soft deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        product.setDeletedAt(Instant.now());
        product.setDeletedBy("system");
        productRepository.save(product);
    }
}
```

#### MapStruct Mapper
```java
package com.tekna.api.product.mapper;

import com.tekna.api.product.dto.CreateProductInput;
import com.tekna.api.product.dto.ProductOutput;
import com.tekna.api.product.dto.UpdateProductInput;
import com.tekna.api.product.persistence.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Product toEntity(CreateProductInput input);

    ProductOutput toOutput(Product entity);

    List<ProductOutput> toOutputList(List<Product> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromInput(UpdateProductInput input, @MappingTarget Product entity);
}
```

---

### 6. Required Dependencies

Add the following dependencies to `pom.xml`:

#### OpenAPI (Swagger)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

#### MapStruct
```xml
<properties>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>
```

And in `maven-compiler-plugin` annotation processor paths:
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>${lombok-mapstruct-binding.version}</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${org.mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

---

### 7. Configuration Profiles (Flyway & Environments)

Configuration is split across profiles in `src/main/resources/`:
- `application.yaml`: Shared settings.
- `application-dev.yaml`: Development configuration.
- `application-prod.yaml`: Production configuration.

#### Database Migrations (Flyway)
- Place SQL scripts in `src/main/resources/db/migration/`.
- **Naming Convention**: `V{version}__{description}.sql` (e.g., `V1__create_products_table.sql`, `V2__add_index_to_products.sql`).
  - Prefix with uppercase `V`.
  - Followed by sequential numbers.
  - Separated by **two underscores** (`__`).
  - Description in lowercase snake_case.
- **Immutability Rule**: Never edit or delete an existing migration once applied. Always create a new version script.
- Audit columns and UUID PK must be defined in all tables using `TIMESTAMP(3)`:

```sql
CREATE TABLE IF NOT EXISTS products (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by VARCHAR(255) NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by VARCHAR(255) NULL
);
```

---

### 8. Exception Handling & Error Standard

#### Standard Error Response Contract
- All API errors return a standard payload matching `ApplicationErrorResponse`.
- For Jakarta validation errors (`MethodArgumentNotValidException`), **only the first validation error message** is returned directly (without concatenating multiple errors or adding extra prefixes).

```java
package com.tekna.api.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationErrorResponse {
    private int code;
    private String message;
    private Instant timestamp;
}
```

#### Application Exception Hierarchy
- [`ApplicationException`](file:///d:/Storage/utp/UTP8/tekna-api/src/main/java/com/tekna/api/common/exception/ApplicationException.java): Base abstract exception holding `HttpStatus`.
- [`NotFoundException`](file:///d:/Storage/utp/UTP8/tekna-api/src/main/java/com/tekna/api/common/exception/NotFoundException.java): Maps to `404 NOT_FOUND`.
- [`BadRequestException`](file:///d:/Storage/utp/UTP8/tekna-api/src/main/java/com/tekna/api/common/exception/BadRequestException.java): Maps to `400 BAD_REQUEST`.
- [`InternalServerException`](file:///d:/Storage/utp/UTP8/tekna-api/src/main/java/com/tekna/api/common/exception/InternalServerException.java): Maps to `500 INTERNAL_SERVER_ERROR`.

#### Global Exception Handler
```java
package com.tekna.api.config;

import com.tekna.api.common.exception.ApplicationErrorResponse;
import com.tekna.api.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApplicationErrorResponse> handleApplicationException(ApplicationException ex) {
        log.warn("Application exception occurred: {}", ex.getMessage());
        ApplicationErrorResponse response = new ApplicationErrorResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApplicationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation error");

        log.warn("Validation failed: {}", errorMessage);

        ApplicationErrorResponse response = new ApplicationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);

        ApplicationErrorResponse response = new ApplicationErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

### 9. Git Commit Convention

- **Format**: `<type>(<scope>): <short description in english>`
- **Language**: English only (maximum 15 words).
- **Allowed Types**:
  - `feat`: New feature or capability (e.g., `feat(product): add search filter endpoint`).
  - `fix`: Bug fix (e.g., `fix(exception): handle missing validation error details`).
  - `ref`: Code refactoring without changing functionality (e.g., `ref(service): optimize product mapping`).
  - `chore`: Build tasks, dependency updates, configuration changes (e.g., `chore(deps): bump mapstruct version`).
  - `perf`: Performance improvements (e.g., `perf(query): add index for soft delete queries`).
  - `docs`: Documentation updates (e.g., `docs(readme): add setup and run guide`).
  - `style`: Formatting, missing semicolons, whitespace (no code change).

