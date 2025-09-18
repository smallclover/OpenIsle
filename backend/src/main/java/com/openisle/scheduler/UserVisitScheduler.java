package com.openisle.scheduler;

import com.openisle.config.CachingConfig;
import com.openisle.model.User;
import com.openisle.model.UserVisit;
import com.openisle.repository.UserRepository;
import com.openisle.repository.UserVisitRepository;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 执行计划
 * 将每天用户访问落库
 * @author smallclover
 * @since 2025-09-09
 */
@Component
@RequiredArgsConstructor
public class UserVisitScheduler {

  private final RedisTemplate redisTemplate;
  private final UserRepository userRepository;
  private final UserVisitRepository userVisitRepository;

  @Scheduled(cron = "0 5 0 * * ?") // 每天 00:05 执行
  public void persistDailyVisits() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String key = CachingConfig.VISIT_CACHE_NAME + ":" + yesterday;
    Set<String> usernames = redisTemplate.opsForSet().members(key);
    if (!CollectionUtils.isEmpty(usernames)) {
      for (String username : usernames) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
          UserVisit userVisit = new UserVisit();
          userVisit.setUser(user);
          userVisit.setVisitDate(yesterday);
          userVisitRepository.save(userVisit);
        }
      }
      redisTemplate.delete(key);
    }
  }
}
