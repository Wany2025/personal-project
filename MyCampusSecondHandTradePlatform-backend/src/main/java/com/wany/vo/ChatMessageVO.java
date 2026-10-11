package com.wany.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;
    private Long productId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private String messageType;
    private LocalDateTime createTime;

    // 发送者信息
    private String senderName;
    private String senderAvatar;
}
