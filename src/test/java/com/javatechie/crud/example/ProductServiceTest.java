package com.javatechie.crud.example;

import com.javatechie.crud.example.ProductException.ProductNotFoundException;
import com.javatechie.crud.example.entity.Product;
import com.javatechie.crud.example.repository.ProductRepository;
import com.javatechie.crud.example.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("saveProduct_whenValidProduct_savesAndReturnsProduct")
    void saveProduct_whenValidProduct_savesAndReturnsProduct() {
        // given
        Product product = new Product(1, "TV", 10, 1000.0);
        when(productRepository.save(product)).thenReturn(product);

        // when
        Product result = productService.saveProduct(product);

        // then
        verify(productRepository).save(product);
        assertNotNull(result);
        assertEquals("TV", result.getName());
    }

    @Test
    @DisplayName("saveProducts_whenValidList_savesAndReturnsProducts")
    void saveProducts_whenValidList_savesAndReturnsProducts() {
        // given
        List<Product> products = Arrays.asList(
                new Product(1, "TV", 10, 1000.0),
                new Product(2, "Laptop", 5, 2000.0)
        );
        when(productRepository.saveAll(products)).thenReturn(products);

        // when
        List<Product> result = productService.saveProducts(products);

        // then
        verify(productRepository).saveAll(products);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getProducts_whenProductsExist_returnsList")
    void getProducts_whenProductsExist_returnsList() {
        // given
        List<Product> products = Collections.singletonList(new Product(1, "TV", 10, 1000.0));
        when(productRepository.findAll()).thenReturn(products);

        // when
        List<Product> result = productService.getProducts();

        // then
        verify(productRepository).findAll();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getProductById_whenProductExists_returnsProduct")
    void getProductById_whenProductExists_returnsProduct() {
        // given
        int id = 1;
        Product product = new Product(id, "TV", 10, 1000.0);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // when
        Product result = productService.getProductById(id);

        // then
        verify(productRepository).findById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("getProductById_whenProductMissing_throwsProductNotFoundException")
    void getProductById_whenProductMissing_throwsProductNotFoundException() {
        // given
        int id = 99;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(id));
        verify(productRepository).findById(id);
    }

    @Test
    @DisplayName("getProductByName_whenProductExists_returnsProduct")
    void getProductByName_whenProductExists_returnsProduct() {
        // given
        String name = "TV";
        Product product = new Product(1, name, 10, 1000.0);
        when(productRepository.findByName(name)).thenReturn(Optional.of(product));

        // when
        Product result = productService.getProductByName(name);

        // then
        verify(productRepository).findByName(name);
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("getProductByName_whenProductMissing_throwsProductNotFoundException")
    void getProductByName_whenProductMissing_throwsProductNotFoundException() {
        // given
        String name = "Unknown";
        when(productRepository.findByName(name)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductByName(name));
        verify(productRepository).findByName(name);
    }

    @Test
    @DisplayName("deleteProduct_whenExistingId_deletesProduct")
    void deleteProduct_whenExistingId_deletesProduct() {
        // given
        int id = 1;
        Product existing = new Product(id, "TV", 10, 1000.0);
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        // when
        String message = productService.deleteProduct(id);

        // then
        verify(productRepository).findById(id);
        verify(productRepository).deleteById(id);
        assertTrue(message.contains("product removed"));
    }

    @Test
    @DisplayName("deleteProduct_whenNonExistingId_throwsProductNotFoundException")
    void deleteProduct_whenNonExistingId_throwsProductNotFoundException() {
        // given
        int id = 99;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(id));
        verify(productRepository).findById(id);
        verify(productRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("updateProduct_whenExistingId_updatesAndReturnsProduct")
    void updateProduct_whenExistingId_updatesAndReturnsProduct() {
        // given
        Product toUpdate = new Product(1, "Updated TV", 20, 1500.0);
        Product existing = new Product(1, "Old TV", 10, 1000.0);

        when(productRepository.findById(1)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        // when
        Product result = productService.updateProduct(toUpdate);

        // then
        verify(productRepository).findById(1);
        verify(productRepository).save(existing);
        assertEquals("Updated TV", result.getName());
        assertEquals(20, result.getQuantity());
        assertEquals(1500.0, result.getPrice());
    }

    @Test
    @DisplayName("updateProduct_whenNonExistingId_throwsProductNotFoundException")
    void updateProduct_whenNonExistingId_throwsProductNotFoundException() {
        // given
        Product toUpdate = new Product(99, "Updated TV", 20, 1500.0);
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(toUpdate));
        verify(productRepository).findById(99);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("generateProductsCsv_whenProductsExist_returnsValidCsv")
    void generateProductsCsv_whenProductsExist_returnsValidCsv() {
        // given
        List<Product> products = Arrays.asList(
                new Product(1, "TV", 10, 1000.0),
                new Product(2, "Laptop", 5, 2000.0)
        );
        when(productRepository.findAll()).thenReturn(products);

        // when
        String csv = productService.generateProductsCsv();

        // then
        verify(productRepository).findAll();
        assertNotNull(csv);
        String[] lines = csv.split("\\r?\\n");
        assertEquals("id,name,quantity,price", lines[0]);
        assertTrue(lines[1].contains("TV"));
    }
}
