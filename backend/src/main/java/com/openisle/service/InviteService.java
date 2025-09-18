package com.openisle.service;

import com.openisle.model.InviteToken;
import com.openisle.model.User;
import com.openisle.repository.InviteTokenRepository;
import com.openisle.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InviteService {

  private final InviteTokenRepository inviteTokenRepository;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PointService pointService;

  @Value
  public class InviteValidateResult {

    InviteToken inviteToken;
    boolean validate;
  }

  public String generate(String username) {
    User inviter = userRepository.findByUsername(username).orElseThrow();
    LocalDate today = LocalDate.now();
    Optional<InviteToken> existing = inviteTokenRepository.findByInviterAndCreatedDate(
      inviter,
      today
    );
    if (existing.isPresent()) {
      InviteToken inviteToken = existing.get();
      return inviteToken.getShortToken() != null
        ? inviteToken.getShortToken()
        : inviteToken.getToken();
    }

    String token = jwtService.generateInviteToken(username);
    String shortToken;
    do {
      shortToken = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    } while (inviteTokenRepository.existsByShortToken(shortToken));

    InviteToken inviteToken = new InviteToken();
    inviteToken.setToken(token);
    inviteToken.setShortToken(shortToken);
    inviteToken.setInviter(inviter);
    inviteToken.setCreatedDate(today);
    inviteToken.setUsageCount(0);
    inviteTokenRepository.save(inviteToken);
    return shortToken;
  }

  public InviteValidateResult validate(String token) {
    if (token == null || token.isEmpty()) {
      return new InviteValidateResult(null, false);
    }

    InviteToken invite = inviteTokenRepository.findById(token).orElse(null);
    String realToken = token;
    if (invite == null) {
      invite = inviteTokenRepository.findByShortToken(token).orElse(null);
      if (invite == null) {
        return new InviteValidateResult(null, false);
      }
      realToken = invite.getToken();
    }

    try {
      jwtService.validateAndGetSubjectForInvite(realToken);
    } catch (Exception e) {
      return new InviteValidateResult(null, false);
    }

    return new InviteValidateResult(invite, invite.getUsageCount() < 3);
  }

  public void consume(String token, String newUserName) {
    InviteToken invite = inviteTokenRepository
      .findById(token)
      .orElseGet(() -> inviteTokenRepository.findByShortToken(token).orElseThrow());
    invite.setUsageCount(invite.getUsageCount() + 1);
    inviteTokenRepository.save(invite);
    pointService.awardForInvite(invite.getInviter().getUsername(), newUserName);
  }
}
