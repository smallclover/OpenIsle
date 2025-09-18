package com.openisle.service;

import com.openisle.exception.FieldException;
import com.openisle.exception.NotFoundException;
import com.openisle.model.PointGood;
import com.openisle.model.PointHistory;
import com.openisle.model.PointHistoryType;
import com.openisle.model.User;
import com.openisle.repository.PointGoodRepository;
import com.openisle.repository.PointHistoryRepository;
import com.openisle.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service for point mall operations. */
@Service
@RequiredArgsConstructor
public class PointMallService {

  private final PointGoodRepository pointGoodRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final PointHistoryRepository pointHistoryRepository;

  public List<PointGood> listGoods() {
    return pointGoodRepository.findAll();
  }

  public int redeem(User user, Long goodId, String contact) {
    PointGood good = pointGoodRepository
      .findById(goodId)
      .orElseThrow(() -> new NotFoundException("Good not found"));
    if (user.getPoint() < good.getCost()) {
      throw new FieldException("point", "Insufficient points");
    }
    user.setPoint(user.getPoint() - good.getCost());
    userRepository.save(user);
    notificationService.createPointRedeemNotifications(user, good.getName() + ": " + contact);
    PointHistory history = new PointHistory();
    history.setUser(user);
    history.setType(PointHistoryType.REDEEM);
    history.setAmount(-good.getCost());
    history.setBalance(user.getPoint());
    history.setCreatedAt(java.time.LocalDateTime.now());
    pointHistoryRepository.save(history);
    return user.getPoint();
  }
}
