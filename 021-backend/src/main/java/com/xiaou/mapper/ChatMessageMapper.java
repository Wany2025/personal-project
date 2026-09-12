package com.xiaou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.entity.ChatMessage;
import com.xiaou.vo.ChatMessageVO;
import com.xiaou.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 会话列表：按 (对方用户, 商品) 聚合，取最新一条消息和未读数
     * currentUserId 作为接收者或发送者都算他的会话
     */
    List<ChatSessionVO> getSessionList(@Param("currentUserId") Long currentUserId);

    /**
     * 聊天记录：两人关于某商品的全部消息（按时间升序，最多 100 条）
     */
    List<ChatMessageVO> getMessages(@Param("currentUserId") Long currentUserId,
                                   @Param("targetUserId") Long targetUserId,
                                   @Param("productId") Long productId);

    /**
     * 未读消息总数
     */
    Integer getUnreadCount(@Param("currentUserId") Long currentUserId);
}
