package com.javatechie.crud.example.service;

import com.javatechie.crud.example.ProductException.ProductNotFoundException;
import com.javatechie.crud.example.entity.Product;
import com.javatechie.crud.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return repository.saveAll(products);
    }

    public List<Product> getProducts() {
        return repository.findAll();
    }

    public Product getProductById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id)
                );

    }

    public Product getProductByName(String name) {
        return repository.findByName(name).orElseThrow(()->
                new ProductNotFoundException("Product not found with name: " + name));
    }

    public String deleteProduct(int id) {
        repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        repository.deleteById(id);
        return "product removed !! " + id;
    }

    public Product updateProduct(Product product) {
        Product existingProduct = repository.findById(product.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + product.getId()));

        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        return repository.save(existingProduct);
    }

    public String generateProductsCsv() {
        List<Product> products = repository.findAll();

        StringWriter stringWriter = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
            // header
            writer.println("id,name,quantity,price");
            for (Product product : products) {
                writer.printf("%d,%s,%d,%.2f%n",
                        product.getId(),
                        escapeCsv(product.getName()),
                        product.getQuantity(),
                        product.getPrice());
            }
        }

        return stringWriter.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        if (needQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

}
