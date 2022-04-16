package com.ecommerce.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.app.dto.request.ProductDetailsRequest;
import com.ecommerce.app.models.Product;
import com.ecommerce.app.services.IProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

	@Autowired
	IProductService productService;

	@Value("${file.upload.location}/products")
	private String location;

	@GetMapping("/latest")
	public ResponseEntity<?> getLatestProducts() {

		List<ProductDetailsRequest> products;
		try (Stream<ProductDetailsRequest> stream = productService.getLatestProducts()) {
			products = stream.limit(7).collect(Collectors.toList());
		}
		return ResponseEntity.ok(products);
	}

	@GetMapping("/most-visited")
	public ResponseEntity<?> getMostVisitedProducts() {

		List<ProductDetailsRequest> products;
		try (Stream<ProductDetailsRequest> stream = productService.getMostVisitedProducts()) {
			products = stream.limit(7).collect(Collectors.toList());
		}
		return ResponseEntity.ok(products);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<?> getProductById(@PathVariable String productId) {
		Product product = productService.getProductById(productId);
		return new ResponseEntity<>(productService.updateVisits(product), HttpStatus.OK);
	}

	// http://localhost:8080/api/products/image/1
	@GetMapping("/image/{productId}")
	// Can be tested with browser. Will work fine with react / angular app.
	public ResponseEntity<byte[]> getFile(@PathVariable String productId) throws IOException {
		Product product = productService.getProductById(productId);
		Path path = Paths.get(location, product.getImage());
		if (Files.exists(path)) {
			byte[] imageData = Files.readAllBytes(path);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + product.getImage() + "\"")
					.body(imageData);
		}

		path = Paths.get(location, "0.jpg");
		byte[] imageData = Files.readAllBytes(path);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "0.jpg" + "\"")
				.body(imageData);
	}
}
