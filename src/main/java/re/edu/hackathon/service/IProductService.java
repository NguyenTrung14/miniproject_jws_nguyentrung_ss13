package re.edu.hackathon.service;

import re.edu.hackathon.dto.request.ProductCreateRequest;
import re.edu.hackathon.dto.request.ProductPatchRequest;
import re.edu.hackathon.dto.request.ProductUpdateRequest;
import re.edu.hackathon.dto.response.ProductPageResponse;
import re.edu.hackathon.dto.response.ProductResponse;

public interface IProductService {
    ProductPageResponse getProducts(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction,
            boolean includeDeleted
    );

    ProductResponse getProduct(Long id);

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    ProductResponse patchProduct(Long id, ProductPatchRequest request);

    void softDeleteProduct(Long id);

    void hardDeleteProduct(Long id);
}
