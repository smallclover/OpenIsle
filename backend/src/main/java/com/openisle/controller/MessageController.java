package com.openisle.controller;

import com.openisle.dto.ConversationDetailDto;
import com.openisle.dto.ConversationDto;
import com.openisle.dto.CreateConversationRequest;
import com.openisle.dto.CreateConversationResponse;
import com.openisle.dto.MessageDto;
import com.openisle.model.Message;
import com.openisle.model.MessageConversation;
import com.openisle.model.User;
import com.openisle.repository.UserRepository;
import com.openisle.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;
  private final UserRepository userRepository;

  // This is a placeholder for getting the current user's ID
  private Long getCurrentUserId(Authentication auth) {
    User user = userRepository
      .findByUsername(auth.getName())
      .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
    // In a real application, you would get this from the Authentication object
    return user.getId();
  }

  @GetMapping("/conversations")
  @Operation(summary = "List conversations", description = "Get all conversations of current user")
  @ApiResponse(
    responseCode = "200",
    description = "List of conversations",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = ConversationDto.class))
    )
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<List<ConversationDto>> getConversations(Authentication auth) {
    List<ConversationDto> conversations = messageService.getConversations(getCurrentUserId(auth));
    return ResponseEntity.ok(conversations);
  }

  @GetMapping("/conversations/{conversationId}")
  @Operation(summary = "Get conversation", description = "Get messages of a conversation")
  @ApiResponse(
    responseCode = "200",
    description = "Conversation detail",
    content = @Content(schema = @Schema(implementation = ConversationDetailDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<ConversationDetailDto> getMessages(
    @PathVariable Long conversationId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    Authentication auth
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    ConversationDetailDto conversationDetails = messageService.getConversationDetails(
      conversationId,
      getCurrentUserId(auth),
      pageable
    );
    return ResponseEntity.ok(conversationDetails);
  }

  @PostMapping
  @Operation(summary = "Send message", description = "Send a direct message to a user")
  @ApiResponse(
    responseCode = "200",
    description = "Message sent",
    content = @Content(schema = @Schema(implementation = MessageDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<MessageDto> sendMessage(
    @RequestBody MessageRequest req,
    Authentication auth
  ) {
    Message message = messageService.sendMessage(
      getCurrentUserId(auth),
      req.getRecipientId(),
      req.getContent(),
      req.getReplyToId()
    );
    return ResponseEntity.ok(messageService.toDto(message));
  }

  @PostMapping("/conversations/{conversationId}/messages")
  @Operation(summary = "Send message to conversation", description = "Reply within a conversation")
  @ApiResponse(
    responseCode = "200",
    description = "Message sent",
    content = @Content(schema = @Schema(implementation = MessageDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<MessageDto> sendMessageToConversation(
    @PathVariable Long conversationId,
    @RequestBody ChannelMessageRequest req,
    Authentication auth
  ) {
    Message message = messageService.sendMessageToConversation(
      getCurrentUserId(auth),
      conversationId,
      req.getContent(),
      req.getReplyToId()
    );
    return ResponseEntity.ok(messageService.toDto(message));
  }

  @PostMapping("/conversations/{conversationId}/read")
  @Operation(
    summary = "Mark conversation read",
    description = "Mark messages in conversation as read"
  )
  @ApiResponse(responseCode = "200", description = "Marked as read")
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId, Authentication auth) {
    messageService.markConversationAsRead(conversationId, getCurrentUserId(auth));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/conversations")
  @Operation(
    summary = "Find or create conversation",
    description = "Find existing or create new conversation with recipient"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Conversation id",
    content = @Content(schema = @Schema(implementation = CreateConversationResponse.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<CreateConversationResponse> findOrCreateConversation(
    @RequestBody CreateConversationRequest req,
    Authentication auth
  ) {
    MessageConversation conversation = messageService.findOrCreateConversation(
      getCurrentUserId(auth),
      req.getRecipientId()
    );
    return ResponseEntity.ok(new CreateConversationResponse(conversation.getId()));
  }

  @GetMapping("/unread-count")
  @Operation(
    summary = "Unread message count",
    description = "Get unread message count for current user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Unread count",
    content = @Content(schema = @Schema(implementation = Long.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<Long> getUnreadCount(Authentication auth) {
    return ResponseEntity.ok(messageService.getUnreadMessageCount(getCurrentUserId(auth)));
  }

  // A simple request DTO
  static class MessageRequest {

    private Long recipientId;
    private String content;
    private Long replyToId;

    public Long getRecipientId() {
      return recipientId;
    }

    public void setRecipientId(Long recipientId) {
      this.recipientId = recipientId;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public Long getReplyToId() {
      return replyToId;
    }

    public void setReplyToId(Long replyToId) {
      this.replyToId = replyToId;
    }
  }

  static class ChannelMessageRequest {

    private String content;
    private Long replyToId;

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public Long getReplyToId() {
      return replyToId;
    }

    public void setReplyToId(Long replyToId) {
      this.replyToId = replyToId;
    }
  }
}
