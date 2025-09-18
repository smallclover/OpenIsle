package com.openisle.controller;

import com.openisle.dto.CategoryDto;
import com.openisle.dto.CategoryRequest;
import com.openisle.dto.PostSummaryDto;
import com.openisle.mapper.CategoryMapper;
import com.openisle.mapper.PostMapper;
import com.openisle.model.Category;
import com.openisle.service.CategoryService;
import com.openisle.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;
  private final PostService postService;
  private final PostMapper postMapper;
  private final CategoryMapper categoryMapper;

  @PostMapping
  @Operation(summary = "Create category", description = "Create a new category")
  @ApiResponse(
    responseCode = "200",
    description = "Created category",
    content = @Content(schema = @Schema(implementation = CategoryDto.class))
  )
  public CategoryDto create(@RequestBody CategoryRequest req) {
    Category c = categoryService.createCategory(
      req.getName(),
      req.getDescription(),
      req.getIcon(),
      req.getSmallIcon()
    );
    long count = postService.countPostsByCategory(c.getId());
    return categoryMapper.toDto(c, count);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Update an existing category")
  @ApiResponse(
    responseCode = "200",
    description = "Updated category",
    content = @Content(schema = @Schema(implementation = CategoryDto.class))
  )
  public CategoryDto update(@PathVariable Long id, @RequestBody CategoryRequest req) {
    Category c = categoryService.updateCategory(
      id,
      req.getName(),
      req.getDescription(),
      req.getIcon(),
      req.getSmallIcon()
    );
    long count = postService.countPostsByCategory(c.getId());
    return categoryMapper.toDto(c, count);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category", description = "Remove a category by id")
  @ApiResponse(responseCode = "200", description = "Category deleted")
  public void delete(@PathVariable Long id) {
    categoryService.deleteCategory(id);
  }

  @GetMapping
  @Operation(summary = "List categories", description = "Get all categories")
  @ApiResponse(
    responseCode = "200",
    description = "List of categories",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))
  )
  public List<CategoryDto> list() {
    List<Category> all = categoryService.listCategories();
    List<Long> ids = all.stream().map(Category::getId).toList();
    Map<Long, Long> postsCntByCategoryIds = postService.countPostsByCategoryIds(ids);
    return all
      .stream()
      .map(c -> categoryMapper.toDto(c, postsCntByCategoryIds.getOrDefault(c.getId(), 0L)))
      .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
      .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category", description = "Get category by id")
  @ApiResponse(
    responseCode = "200",
    description = "Category detail",
    content = @Content(schema = @Schema(implementation = CategoryDto.class))
  )
  public CategoryDto get(@PathVariable Long id) {
    Category c = categoryService.getCategory(id);
    long count = postService.countPostsByCategory(c.getId());
    return categoryMapper.toDto(c, count);
  }

  @GetMapping("/{id}/posts")
  @Operation(summary = "List posts by category", description = "Get posts under a category")
  @ApiResponse(
    responseCode = "200",
    description = "List of posts",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PostSummaryDto.class))
    )
  )
  public List<PostSummaryDto> listPostsByCategory(
    @PathVariable Long id,
    @RequestParam(value = "page", required = false) Integer page,
    @RequestParam(value = "pageSize", required = false) Integer pageSize
  ) {
    return postService
      .listPostsByCategories(java.util.List.of(id), page, pageSize)
      .stream()
      .map(postMapper::toSummaryDto)
      .collect(Collectors.toList());
  }
}
