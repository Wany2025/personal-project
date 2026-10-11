package com.wany.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {

    // 对方用户ID（会话另一方）
    private Long targetUserId;
    private String targetName;
    private String targetAvatar;

    // 商品信息
    private Long productId;
    private String productTitle;
    private String productImage;
    private String productStatus;

    // 最后一条消息
    private String lastMessage;
    private LocalDateTime lastTime;

    // 未读数
    private Integer unreadCount;
}
