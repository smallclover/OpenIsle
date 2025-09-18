package com.openisle.service;

import com.openisle.dto.TelegramLoginRequest;
import com.openisle.model.RegisterMode;
import com.openisle.model.Role;
import com.openisle.model.User;
import com.openisle.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

  private final UserRepository userRepository;
  private final AvatarGenerator avatarGenerator;

  @Value("${telegram.bot-token:}")
  private String botToken;

  public Optional<AuthResult> authenticate(
    TelegramLoginRequest req,
    RegisterMode mode,
    boolean viaInvite
  ) {
    try {
      if (botToken == null || botToken.isEmpty()) {
        return Optional.empty();
      }
      String dataCheckString = buildDataCheckString(req);
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] secretKey = md.digest(botToken.getBytes(StandardCharsets.UTF_8));
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
      byte[] hash = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
      String hex = bytesToHex(hash);
      if (!hex.equalsIgnoreCase(req.getHash())) {
        return Optional.empty();
      }
      String username = req.getUsername();
      String email = (username != null ? username : req.getId()) + "@telegram.org";
      String avatar = req.getPhotoUrl();
      return Optional.of(processUser(email, username, avatar, mode, viaInvite));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String buildDataCheckString(TelegramLoginRequest req) {
    List<String> data = new ArrayList<>();
    if (req.getAuthDate() != null) data.add("auth_date=" + req.getAuthDate());
    if (req.getFirstName() != null) data.add("first_name=" + req.getFirstName());
    if (req.getId() != null) data.add("id=" + req.getId());
    if (req.getLastName() != null) data.add("last_name=" + req.getLastName());
    if (req.getPhotoUrl() != null) data.add("photo_url=" + req.getPhotoUrl());
    if (req.getUsername() != null) data.add("username=" + req.getUsername());
    Collections.sort(data);
    return String.join("\n", data);
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private AuthResult processUser(
    String email,
    String username,
    String avatar,
    RegisterMode mode,
    boolean viaInvite
  ) {
    Optional<User> existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      User user = existing.get();
      if (!user.isVerified()) {
        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
      }
      return new AuthResult(user, false);
    }
    String baseUsername = username != null ? username : email.split("@")[0];
    String finalUsername = baseUsername;
    int suffix = 1;
    while (userRepository.findByUsername(finalUsername).isPresent()) {
      finalUsername = baseUsername + suffix++;
    }
    User user = new User();
    user.setUsername(finalUsername);
    user.setEmail(email);
    user.setPassword("");
    user.setRole(Role.USER);
    user.setVerified(true);
    user.setApproved(mode == RegisterMode.DIRECT || viaInvite);
    if (avatar != null) {
      user.setAvatar(avatar);
    } else {
      user.setAvatar(avatarGenerator.generate(finalUsername));
    }
    return new AuthResult(userRepository.save(user), true);
  }
}
