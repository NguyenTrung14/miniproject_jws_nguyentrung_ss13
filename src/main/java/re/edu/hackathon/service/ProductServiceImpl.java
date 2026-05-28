package re.edu.hackathon.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.edu.hackathon.dto.request.ProductCreateRequest;
import re.edu.hackathon.dto.request.ProductPatchRequest;
import re.edu.hackathon.dto.request.ProductUpdateRequest;
import re.edu.hackathon.dto.response.ProductPageResponse;
import re.edu.hackathon.dto.response.ProductResponse;
import re.edu.hackathon.entity.Product;
import re.edu.hackathon.exception.CustomException;
import re.edu.hackathon.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "code",
            "name",
            "sku",
            "stockQuantity",
            "salePrice",
            "deleted",
            "createdAt",
            "updatedAt"
    );

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse getProducts(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction,
            boolean includeDeleted
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Product> products = productRepository.findAll(buildSpecification(keyword, includeDeleted), pageable);
        List<ProductResponse> content = products.getContent().stream()
                .map(this::toResponse)
                .toList();

        return ProductPageResponse.builder()
                .content(content)
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .last(products.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(findActiveProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        String code = normalize(request.getCode());
        String sku = normalize(request.getSku());
        ensureCodeAvailable(code, null);
        ensureSkuAvailable(sku, null);

        Product product = Product.builder()
                .code(code)
                .name(normalize(request.getName()))
                .sku(sku)
                .detail(normalize(request.getDetail()))
                .stockQuantity(request.getStockQuantity())
                .salePrice(request.getSalePrice())
                .deleted(false)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findActiveProduct(id);
        String code = normalize(request.getCode());
        String sku = normalize(request.getSku());
        ensureCodeAvailable(code, id);
        ensureSkuAvailable(sku, id);

        product.setCode(code);
        product.setName(normalize(request.getName()));
        product.setSku(sku);
        product.setDetail(normalize(request.getDetail()));
        product.setStockQuantity(request.getStockQuantity());
        product.setSalePrice(request.getSalePrice());

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse patchProduct(Long id, ProductPatchRequest request) {
        if (request.isEmpty()) {
            throw CustomException.badRequest("Cần cung cấp ít nhất một trường để cập nhật một phần");
        }

        Product product = findActiveProduct(id);

        if (request.getCode() != null) {
            String code = normalize(request.getCode());
            ensureCodeAvailable(code, id);
            product.setCode(code);
        }
        if (request.getName() != null) {
            product.setName(normalize(request.getName()));
        }
        if (request.getSku() != null) {
            String sku = normalize(request.getSku());
            ensureSkuAvailable(sku, id);
            product.setSku(sku);
        }
        if (request.getDetail() != null) {
            product.setDetail(normalize(request.getDetail()));
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getSalePrice() != null) {
            product.setSalePrice(request.getSalePrice());
        }

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void softDeleteProduct(Long id) {
        Product product = findActiveProduct(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void hardDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Không tìm thấy sản phẩm với id: " + id));
        productRepository.delete(product);
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findById(id)
                .filter(product -> !Boolean.TRUE.equals(product.getDeleted()))
                .orElseThrow(() -> CustomException.notFound("Không tìm thấy sản phẩm với id: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw CustomException.badRequest("Số trang phải lớn hơn hoặc bằng 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw CustomException.badRequest("Kích thước trang phải nằm trong khoảng từ 1 đến " + MAX_PAGE_SIZE);
        }

        String normalizedSortBy = sortBy == null || sortBy.isBlank() ? "id" : sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
            throw CustomException.badRequest("Trường sắp xếp không được hỗ trợ: " + normalizedSortBy);
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction == null ? "asc" : direction.trim());
        } catch (IllegalArgumentException ex) {
            throw CustomException.badRequest("Hướng sắp xếp phải là asc hoặc desc");
        }

        return PageRequest.of(page, size, Sort.by(sortDirection, normalizedSortBy));
    }

    private Specification<Product> buildSpecification(String keyword, boolean includeDeleted) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!includeDeleted) {
                predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("detail")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void ensureCodeAvailable(String code, Long excludedId) {
        boolean exists = excludedId == null
                ? productRepository.existsByCode(code)
                : productRepository.existsByCodeAndIdNot(code, excludedId);
        if (exists) {
            throw CustomException.conflict("Mã sản phẩm đã tồn tại: " + code);
        }
    }

    private void ensureSkuAvailable(String sku, Long excludedId) {
        boolean exists = excludedId == null
                ? productRepository.existsBySku(sku)
                : productRepository.existsBySkuAndIdNot(sku, excludedId);
        if (exists) {
            throw CustomException.conflict("SKU đã tồn tại: " + sku);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .sku(product.getSku())
                .detail(product.getDetail())
                .stockQuantity(product.getStockQuantity())
                .salePrice(product.getSalePrice())
                .deleted(product.getDeleted())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
