package emamura_ec.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import emamura_ec.entity.Product;
import emamura_ec.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;    

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
}
