package com.wany.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wany.dto.ChatSendDTO;
import com.wany.entity.ChatMessage;
import com.wany.entity.Product;
import com.wany.exception.BusinessException;
import com.wany.mapper.ChatMessageMapper;
import com.wany.mapper.ProductMapper;
import com.wany.service.ChatService;
import com.wany.vo.ChatMessageVO;
import com.wany.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void sendMessage(Long senderId, ChatSendDTO sendDTO) {
        // 内容去空格后非空校验（DTO 已有 @NotBlank，这里再防御一下）
        if (StrUtil.isBlank(sendDTO.getContent())) {
            throw new BusinessException("消息内容不能为空");
        }

        // 不能给自己发消息
        if (senderId.equals(sendDTO.getReceiverId())) {
            throw new BusinessException("不能给自己发消息");
        }

        // 校验商品存在
        Product product = productMapper.selectById(sendDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 关键校验：必须有一方是该商品的卖家
        Long receiverId = sendDTO.getReceiverId();
        boolean senderIsSeller = product.getSellerId().equals(senderId);
        boolean receiverIsSeller = product.getSellerId().equals(receiverId);
        if (!senderIsSeller && !receiverIsSeller) {
            throw new BusinessException("只能与商品卖家进行沟通");
        }

        ChatMessage message = new ChatMessage();
        message.setProductId(sendDTO.getProductId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(sendDTO.getContent().trim());
        message.setIsRead(0);
        message.setMessageType("text");
        chatMessageMapper.insert(message);
    }

    @Override
    public List<ChatSessionVO> getSessionList(Long currentUserId) {
        return chatMessageMapper.getSessionList(currentUserId);
    }

    @Override
    public List<ChatMessageVO> getMessages(Long currentUserId, Long targetUserId, Long productId) {
        if (targetUserId == null || productId == null) {
            throw new BusinessException("目标用户ID和商品ID不能为空");
        }
        List<ChatMessageVO> list = chatMessageMapper.getMessages(currentUserId, targetUserId, productId);
        // SQL 按 create_time DESC 取出，前端展示需升序 → 反转
        java.util.Collections.reverse(list);
        return list;
    }

    @Override
    @Transactional
    public void markRead(Long currentUserId, Long targetUserId, Long productId) {
        // 批量更新：对方发给我的、关于该商品的、未读消息 → 已读
        chatMessageMapper.update(null,
                new LambdaUpdateWrapper<ChatMessage>()
                        .eq(ChatMessage::getReceiverId, currentUserId)
                        .eq(ChatMessage::getSenderId, targetUserId)
                        .eq(ChatMessage::getProductId, productId)
                        .eq(ChatMessage::getIsRead, 0)
                        .set(ChatMessage::getIsRead, 1));
    }

    @Override
    public Integer getUnreadCount(Long currentUserId) {
        Integer count = chatMessageMapper.getUnreadCount(currentUserId);
        return count == null ? 0 : count;
    }
}
