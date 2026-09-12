package com.xiaou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.entity.Favorite;
import com.xiaou.entity.Product;
import com.xiaou.exception.BusinessException;
import com.xiaou.mapper.FavoriteMapper;
import com.xiaou.mapper.ProductMapper;
import com.xiaou.service.FavoriteService;
import com.xiaou.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 不能收藏自己的商品
        if (product.getSellerId().equals(userId)) {
            throw new BusinessException("不能收藏自己的商品");
        }

        // 防重复：已收藏则直接返回（幂等）
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);

        // 同步商品收藏数 +1
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .setSql("favorite_count = favorite_count + 1"));
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        int deleted = favoriteMapper.delete(wrapper);

        if (deleted > 0) {
            // 收藏数 -1，但不低于 0
            productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                            .eq(Product::getId, productId)
                            .setSql("favorite_count = GREATEST(favorite_count - 1, 0)"));
        }
    }

    @Override
    public Page<FavoriteVO> getMyFavorites(Long userId, Integer current, Integer size) {
        Page<FavoriteVO> page = new Page<>(current, size);
        return favoriteMapper.getFavoriteList(page, userId);
    }

    @Override
    public Boolean isFavorited(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
