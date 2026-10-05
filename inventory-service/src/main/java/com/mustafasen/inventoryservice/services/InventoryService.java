package com.mustafasen.inventoryservice.services;

import com.mustafasen.inventoryservice.dtos.ProductRequest;
import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.entities.Product;
import com.mustafasen.inventoryservice.exceptions.ProductAlreadyExistsException;
import com.mustafasen.inventoryservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Seeds the demo products only into an empty table, so restarts against a
    // persistent DB don't overwrite edited data.
    @Transactional
    public void seedData() {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.save(new Product("product-1", "Product 1", 50));
        productRepository.save(new Product("product-2", "Product 2", 10));
        productRepository.save(new Product("product-3", "Product 3", 0));
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Unknown productId: " + productId));
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        validate(request);
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (productRepository.existsById(request.getId())) {
            throw new ProductAlreadyExistsException(request.getId());
        }
        return productRepository.save(new Product(request.getId(), request.getName(), request.getQuantity()));
    }

    @Transactional
    public Product updateProduct(String productId, ProductRequest request) {
        validate(request);
        Product product = getProduct(productId);
        product.setName(request.getName());
        product.setQuantity(request.getQuantity());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("Unknown productId: " + productId);
        }
        productRepository.deleteById(productId);
    }

    public StockResponse getStock(String productId) {
        Product product = getProduct(productId);
        return new StockResponse(product.getId(), product.getQuantity());
    }

    @Transactional
    public ReservationResponse reserve(String productId, int requestedQuantity) {
        Optional<Product> found = productRepository.findByIdForUpdate(productId);
        if (found.isEmpty()) {
            return new ReservationResponse(false, "Unknown productId: " + productId, 0);
        }
        Product product = found.get();
        int available = product.getQuantity();
        if (available < requestedQuantity) {
            return new ReservationResponse(false, "Insufficient stock", available);
        }
        int remaining = available - requestedQuantity;
        product.setQuantity(remaining);
        productRepository.save(product);
        return new ReservationResponse(true, "Reserved", remaining);
    }

    private void validate(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
    }
}
