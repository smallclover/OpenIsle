package com.openisle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openisle.model.PointHistory;
import com.openisle.model.PointHistoryType;
import com.openisle.model.Role;
import com.openisle.model.User;
import com.openisle.repository.PointHistoryRepository;
import com.openisle.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(PointService.class)
class PointServiceRecalculateUserPointsTest {

  @Autowired
  private PointService pointService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PointHistoryRepository pointHistoryRepository;

  @Test
  void recalculatesBalanceAfterDeletion() {
    User user = new User();
    user.setUsername("u");
    user.setEmail("u@example.com");
    user.setPassword("p");
    user.setRole(Role.USER);
    userRepository.save(user);

    PointHistory h1 = new PointHistory();
    h1.setUser(user);
    h1.setType(PointHistoryType.POST);
    h1.setAmount(30);
    h1.setBalance(30);
    h1.setCreatedAt(LocalDateTime.now().minusMinutes(2));
    pointHistoryRepository.save(h1);

    PointHistory h2 = new PointHistory();
    h2.setUser(user);
    h2.setType(PointHistoryType.COMMENT);
    h2.setAmount(10);
    h2.setBalance(40);
    h2.setCreatedAt(LocalDateTime.now().minusMinutes(1));
    pointHistoryRepository.save(h2);

    user.setPoint(40);
    userRepository.save(user);

    pointHistoryRepository.delete(h1);

    int total = pointService.recalculateUserPoints(user);

    assertEquals(10, total);
    assertEquals(10, userRepository.findById(user.getId()).orElseThrow().getPoint());
    assertEquals(10, pointHistoryRepository.findById(h2.getId()).orElseThrow().getBalance());
  }
}
