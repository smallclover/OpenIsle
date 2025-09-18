package com.openisle.repository;

import com.openisle.model.User;
import com.openisle.model.UserSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
  List<UserSubscription> findBySubscriber(User subscriber);
  List<UserSubscription> findByTarget(User target);
  Optional<UserSubscription> findBySubscriberAndTarget(User subscriber, User target);
  long countByTarget(User target);
  long countBySubscriber(User subscriber);
}
