package com.openisle.controller;

import com.openisle.model.Notification;
import com.openisle.model.NotificationType;
import com.openisle.model.User;
import com.openisle.repository.NotificationRepository;
import com.openisle.repository.UserRepository;
import com.openisle.service.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final EmailSender emailSender;

  @Value("${app.website-url}")
  private String websiteUrl;

  @PostMapping("/{id}/approve")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Approve user", description = "Approve a pending user registration")
  @ApiResponse(responseCode = "200", description = "User approved")
  public ResponseEntity<?> approve(@PathVariable Long id) {
    User user = userRepository.findById(id).orElseThrow();
    user.setApproved(true);
    userRepository.save(user);
    markRegisterRequestNotificationsRead(user);
    emailSender.sendEmail(
      user.getEmail(),
      "您的注册已审核通过",
      "🎉您的注册已经审核通过, 点击以访问网站: " + websiteUrl
    );
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/reject")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Reject user", description = "Reject a pending user registration")
  @ApiResponse(responseCode = "200", description = "User rejected")
  public ResponseEntity<?> reject(@PathVariable Long id) {
    User user = userRepository.findById(id).orElseThrow();
    user.setApproved(false);
    userRepository.save(user);
    markRegisterRequestNotificationsRead(user);
    emailSender.sendEmail(
      user.getEmail(),
      "您的注册已被管理员拒绝",
      "您的注册被管理员拒绝, 点击链接可以重新填写理由申请: " + websiteUrl
    );
    return ResponseEntity.ok().build();
  }

  private void markRegisterRequestNotificationsRead(User applicant) {
    java.util.List<Notification> notifs = notificationRepository.findByTypeAndFromUser(
      NotificationType.REGISTER_REQUEST,
      applicant
    );
    for (Notification n : notifs) {
      n.setRead(true);
    }
    notificationRepository.saveAll(notifs);
  }
}
