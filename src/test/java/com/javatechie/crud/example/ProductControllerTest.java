package com.javatechie.crud.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.crud.example.ProductException.GlobalExceptionHandler;
import com.javatechie.crud.example.ProductException.ProductNotFoundException;
import com.javatechie.crud.example.entity.Product;
import com.javatechie.crud.example.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.javatechie.crud.example.controller.ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("addProduct_whenValidRequest_returnsCreatedProduct")
    void addProduct_whenValidRequest_returnsCreatedProduct() throws Exception {
        // given
        Product request = new Product(0, "TV", 10, 1000.0);
        Product response = new Product(1, "TV", 10, 1000.0);
        when(productService.saveProduct(any(Product.class))).thenReturn(response);

        // when / then
        mockMvc.perform(post("/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("TV")));
    }

    @Test
    @DisplayName("getProducts_whenProductsExist_returnsList")
    void getProducts_whenProductsExist_returnsList() throws Exception {
        // given
        Product p1 = new Product(1, "TV", 10, 1000.0);
        when(productService.getProducts()).thenReturn(Collections.singletonList(p1));

        // when / then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("getProductById_whenProductExists_returnsProduct")
    void getProductById_whenProductExists_returnsProduct() throws Exception {
        // given
        Product product = new Product(1, "TV", 10, 1000.0);
        when(productService.getProductById(1)).thenReturn(product);

        // when / then
        mockMvc.perform(get("/productById/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("TV")));
    }

    @Test
    @DisplayName("getProductById_whenProductMissing_returnsErrorResponse")
    void getProductById_whenProductMissing_returnsErrorResponse() throws Exception {
        // given
        when(productService.getProductById(99)).thenThrow(new ProductNotFoundException("Product not found with id: 99"));

        // when / then
        mockMvc.perform(get("/productById/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 99")))
                .andExpect(jsonPath("$.path", is("/productById/99")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("getProductByName_whenProductExists_returnsProduct")
    void getProductByName_whenProductExists_returnsProduct() throws Exception {
        // given
        Product product = new Product(1, "TV", 10, 1000.0);
        when(productService.getProductByName("TV")).thenReturn(product);

        // when / then
        mockMvc.perform(get("/product/{name}", "TV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("TV")));
    }

    @Test
    @DisplayName("deleteProduct_whenExistingId_returnsSuccessMessage")
    void deleteProduct_whenExistingId_returnsSuccessMessage() throws Exception {
        // given
        when(productService.deleteProduct(1)).thenReturn("product removed !! 1");

        // when / then
        mockMvc.perform(delete("/delete/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("product removed")));
    }

    @Test
    @DisplayName("deleteProduct_whenNonExistingId_returnsErrorResponse")
    void deleteProduct_whenNonExistingId_returnsErrorResponse() throws Exception {
        // given
        when(productService.deleteProduct(99)).thenThrow(new ProductNotFoundException("Product not found with id: 99"));

        // when / then
        mockMvc.perform(delete("/delete/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 99")))
                .andExpect(jsonPath("$.path", is("/delete/99")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("updateProduct_whenValidRequest_returnsUpdatedProduct")
    void updateProduct_whenValidRequest_returnsUpdatedProduct() throws Exception {
        // given
        Product request = new Product(1, "Updated TV", 20, 1500.0);
        Product response = new Product(1, "Updated TV", 20, 1500.0);
        when(productService.updateProduct(Mockito.any(Product.class))).thenReturn(response);

        // when / then
        mockMvc.perform(put("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated TV")))
                .andExpect(jsonPath("$.quantity", is(20)));
    }

    @Test
    @DisplayName("downloadProductsCsv_whenProductsExist_returnsCsvFile")
    void downloadProductsCsv_whenProductsExist_returnsCsvFile() throws Exception {
        // given
        String csvContent = "id,name,quantity,price\n1,TV,10,1000.00";
        when(productService.generateProductsCsv()).thenReturn(csvContent);

        // when / then
        mockMvc.perform(get("/products/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("products.csv")))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("id,name,quantity,price")));
    }
}

