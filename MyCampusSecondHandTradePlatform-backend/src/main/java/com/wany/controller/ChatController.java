package com.wany.controller;

import com.wany.common.Result;
import com.wany.dto.ChatSendDTO;
import com.wany.service.ChatService;
import com.wany.vo.ChatMessageVO;
import com.wany.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public Result<String> sendMessage(@RequestAttribute("userId") Long userId,
                                     @Valid @RequestBody ChatSendDTO sendDTO) {
        chatService.sendMessage(userId, sendDTO);
        return Result.success("发送成功");
    }

    @GetMapping("/list")
    public Result<List<ChatSessionVO>> getSessionList(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.getSessionList(userId));
    }

    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> getMessages(@RequestAttribute("userId") Long userId,
                                                  @RequestParam Long targetUserId,
                                                  @RequestParam Long productId) {
        return Result.success(chatService.getMessages(userId, targetUserId, productId));
    }

    @PutMapping("/read")
    public Result<String> markRead(@RequestAttribute("userId") Long userId,
                                   @RequestParam Long targetUserId,
                                   @RequestParam Long productId) {
        chatService.markRead(userId, targetUserId, productId);
        return Result.success("已标记已读");
    }

    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.getUnreadCount(userId));
    }
}
