package com.xiaou.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FavoriteVO {

    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime createTime;

    // 商品快照信息
    private String title;
    private BigDecimal price;
    private String images;
    private String status;
    private String condition;

    // 卖家信息
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;

    // 商品图片列表
    public List<String> getImageList() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return List.of(images.split(","));
    }
}
