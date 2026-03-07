package com.github.solairerove.darkkitchen.api.product;

import com.github.solairerove.darkkitchen.api.common.exception.NotFoundException;
import com.github.solairerove.darkkitchen.api.product.dto.ProductRequest;
import com.github.solairerove.darkkitchen.api.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findAllByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(getProductEntity(id));
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        apply(request, product);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProductEntity(id);
        apply(request, product);
        return toResponse(productRepository.save(product));
    }

    public void deactivateProduct(Long id) {
        Product product = getProductEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getActiveProduct(Long id) {
        Product product = getProductEntity(id);
        if (!product.isActive()) {
            throw new NotFoundException("Product not found");
        }
        return product;
    }

    private Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void apply(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(BigDecimal.valueOf(request.price()));
        product.setUnit(request.unit());
        product.setActive(request.active() == null || request.active());
        product.setSortOrder(request.sortOrder());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().longValue(),
                product.getUnit(),
                product.isActive(),
                product.getSortOrder()
        );
    }
}
