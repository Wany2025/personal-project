package com.xiaou.service;

import com.xiaou.dto.ChatSendDTO;
import com.xiaou.vo.ChatMessageVO;
import com.xiaou.vo.ChatSessionVO;

import java.util.List;

public interface ChatService {

    /**
     * 发送文本消息
     */
    void sendMessage(Long senderId, ChatSendDTO sendDTO);

    /**
     * 会话列表
     */
    List<ChatSessionVO> getSessionList(Long currentUserId);

    /**
     * 聊天记录（按时间升序返回，最近100条）
     */
    List<ChatMessageVO> getMessages(Long currentUserId, Long targetUserId, Long productId);

    /**
     * 标记某会话消息为已读
     */
    void markRead(Long currentUserId, Long targetUserId, Long productId);

    /**
     * 当前用户未读消息总数
     */
    Integer getUnreadCount(Long currentUserId);
}
