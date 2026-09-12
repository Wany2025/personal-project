package com.xiaou.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatSendDTO {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
