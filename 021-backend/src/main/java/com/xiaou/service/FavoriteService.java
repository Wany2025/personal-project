package com.xiaou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.vo.FavoriteVO;

public interface FavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long productId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long productId);

    /**
     * 我的收藏列表（分页）
     */
    Page<FavoriteVO> getMyFavorites(Long userId, Integer current, Integer size);

    /**
     * 检查是否已收藏
     */
    Boolean isFavorited(Long userId, Long productId);
}
