package com.wany.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wany.dto.ProductPublishDTO;
import com.wany.dto.ProductQueryDTO;
import com.wany.vo.ProductDetailVO;
import com.wany.vo.ProductVO;

public interface ProductService {

    Long publishProduct(Long userId, ProductPublishDTO publishDTO);

    void updateProduct(Long userId, Long productId, ProductPublishDTO updateDTO);

    void deleteProduct(Long userId, Long productId);

    void changeProductStatus(Long userId, Long productId, String status);

    Page<ProductVO> getProductList(ProductQueryDTO queryDTO);

    ProductDetailVO getProductDetail(Long productId);

    Page<ProductVO> getMyProducts(Long userId, Integer current, Integer size);
}