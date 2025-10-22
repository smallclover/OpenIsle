package com.openisle.service;

import com.openisle.dto.DonationDto;
import com.openisle.dto.DonationResponse;
import com.openisle.exception.FieldException;
import com.openisle.model.*;
import com.openisle.repository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

  private final UserRepository userRepository;
  private final PointLogRepository pointLogRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final NotificationService notificationService;
  private final PostChangeLogService postChangeLogService;

  public int awardForPost(String userName, Long postId) {
    User user = userRepository.findByUsername(userName).orElseThrow();
    PointLog log = getTodayLog(user);
    if (log.getPostCount() > 1) return 0;
    log.setPostCount(log.getPostCount() + 1);
    pointLogRepository.save(log);
    Post post = postRepository.findById(postId).orElseThrow();
    return addPoint(user, 30, PointHistoryType.POST, post, null, null);
  }

  public int awardForInvite(String userName, String inviteeName) {
    User user = userRepository.findByUsername(userName).orElseThrow();
    User invitee = userRepository.findByUsername(inviteeName).orElseThrow();
    return addPoint(user, 500, PointHistoryType.INVITE, null, null, invitee);
  }

  public int awardForFeatured(String userName, Long postId) {
    User user = userRepository.findByUsername(userName).orElseThrow();
    Post post = postRepository.findById(postId).orElseThrow();
    return addPoint(user, 500, PointHistoryType.FEATURE, post, null, null);
  }

  public void processLotteryJoin(User participant, LotteryPost post) {
    int cost = post.getPointCost();
    if (cost > 0) {
      if (participant.getPoint() < cost) {
        throw new FieldException("point", "积分不足");
      }
      addPoint(participant, -cost, PointHistoryType.LOTTERY_JOIN, post, null, post.getAuthor());
      addPoint(post.getAuthor(), cost, PointHistoryType.LOTTERY_REWARD, post, null, participant);
    }
  }

  private PointLog getTodayLog(User user) {
    LocalDate today = LocalDate.now();
    return pointLogRepository
      .findByUserAndLogDate(user, today)
      .orElseGet(() -> {
        PointLog log = new PointLog();
        log.setUser(user);
        log.setLogDate(today);
        log.setPostCount(0);
        log.setCommentCount(0);
        log.setReactionCount(0);
        return pointLogRepository.save(log);
      });
  }

  private int addPoint(
    User user,
    int amount,
    PointHistoryType type,
    Post post,
    Comment comment,
    User fromUser
  ) {
    if (pointHistoryRepository.countByUser(user) == 0) {
      recordHistory(user, PointHistoryType.SYSTEM_ONLINE, 0, null, null, null);
    }
    user.setPoint(user.getPoint() + amount);
    userRepository.save(user);
    recordHistory(user, type, amount, post, comment, fromUser);
    return amount;
  }

  private void recordHistory(
    User user,
    PointHistoryType type,
    int amount,
    Post post,
    Comment comment,
    User fromUser
  ) {
    PointHistory history = new PointHistory();
    history.setUser(user);
    history.setType(type);
    history.setAmount(amount);
    history.setBalance(user.getPoint());
    history.setPost(post);
    history.setComment(comment);
    history.setFromUser(fromUser);
    history.setCreatedAt(java.time.LocalDateTime.now());
    pointHistoryRepository.save(history);
  }

  // 同时为评论者和发帖人增加积分，返回值为评论者增加的积分数
  // 注意需要考虑发帖和回复是同一人的场景
  public int awardForComment(String commenterName, Long postId, Long commentId) {
    // 标记评论者是否已达到积分奖励上限
    boolean isTheRewardCapped = false;

    // 根据帖子id找到发帖人
    Post post = postRepository.findById(postId).orElseThrow();
    User poster = post.getAuthor();
    Comment comment = commentRepository.findById(commentId).orElseThrow();

    // 获取评论者的加分日志
    User commenter = userRepository.findByUsername(commenterName).orElseThrow();
    PointLog log = getTodayLog(commenter);
    if (log.getCommentCount() > 3) {
      isTheRewardCapped = true;
    }

    // 如果发帖人与评论者是同一个，则只计算单次加分
    if (poster.getId().equals(commenter.getId())) {
      if (isTheRewardCapped) {
        return 0;
      } else {
        log.setCommentCount(log.getCommentCount() + 1);
        pointLogRepository.save(log);
        return addPoint(commenter, 10, PointHistoryType.COMMENT, post, comment, null);
      }
    } else {
      addPoint(poster, 10, PointHistoryType.COMMENT, post, comment, commenter);
      // 如果发帖人与评论者不是同一个，则根据是否达到积分上限来判断评论者加分情况
      if (isTheRewardCapped) {
        return 0;
      } else {
        return addPoint(commenter, 10, PointHistoryType.COMMENT, post, comment, null);
      }
    }
  }

  // 需要考虑点赞者和发帖人是同一个的情况
  public int awardForReactionOfPost(String reactionerName, Long postId) {
    // 根据帖子id找到发帖人
    User poster = postRepository.findById(postId).orElseThrow().getAuthor();

    // 获取点赞者信息
    User reactioner = userRepository.findByUsername(reactionerName).orElseThrow();

    // 如果发帖人与点赞者是同一个，则不加分
    if (poster.getId().equals(reactioner.getId())) {
      return 0;
    }

    // 如果不是同一个，则为发帖人加分
    Post post = postRepository.findById(postId).orElseThrow();
    return addPoint(poster, 10, PointHistoryType.POST_LIKED, post, null, reactioner);
  }

  public int deductForReactionOfPost(String reactionerName, Long postId) {
    User poster = postRepository.findById(postId).orElseThrow().getAuthor();
    User reactioner = userRepository.findByUsername(reactionerName).orElseThrow();
    if (poster.getId().equals(reactioner.getId())) {
      return 0;
    }
    Post post = postRepository.findById(postId).orElseThrow();
    return addPoint(poster, -10, PointHistoryType.POST_LIKE_CANCELLED, post, null, reactioner);
  }

  // 考虑点赞者和评论者是同一个的情况
  public int awardForReactionOfComment(String reactionerName, Long commentId) {
    // 根据帖子id找到评论者
    User commenter = commentRepository.findById(commentId).orElseThrow().getAuthor();

    // 获取点赞者信息
    User reactioner = userRepository.findByUsername(reactionerName).orElseThrow();

    // 如果评论者与点赞者是同一个，则不加分
    if (commenter.getId().equals(reactioner.getId())) {
      return 0;
    }

    // 如果不是同一个，则为发帖人加分
    Comment comment = commentRepository.findById(commentId).orElseThrow();
    Post post = comment.getPost();
    return addPoint(commenter, 10, PointHistoryType.COMMENT_LIKED, post, comment, reactioner);
  }

  public int deductForReactionOfComment(String reactionerName, Long commentId) {
    User commenter = commentRepository.findById(commentId).orElseThrow().getAuthor();
    User reactioner = userRepository.findByUsername(reactionerName).orElseThrow();
    if (commenter.getId().equals(reactioner.getId())) {
      return 0;
    }
    Comment comment = commentRepository.findById(commentId).orElseThrow();
    Post post = comment.getPost();
    return addPoint(
      commenter,
      -10,
      PointHistoryType.COMMENT_LIKE_CANCELLED,
      post,
      comment,
      reactioner
    );
  }

  public java.util.List<PointHistory> listHistory(String userName) {
    User user = userRepository.findByUsername(userName).orElseThrow();
    if (pointHistoryRepository.countByUser(user) == 0) {
      recordHistory(user, PointHistoryType.SYSTEM_ONLINE, 0, null, null, null);
    }
    return pointHistoryRepository.findByUserOrderByIdDesc(user);
  }

  public List<Map<String, Object>> trend(String userName, int days) {
    if (days < 1) days = 1;
    User user = userRepository.findByUsername(userName).orElseThrow();
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(days - 1L);
    var histories = pointHistoryRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
      user,
      start.atStartOfDay()
    );
    int idx = 0;
    int balance = user.getPoint();
    List<Map<String, Object>> result = new ArrayList<>();
    for (LocalDate day = end; !day.isBefore(start); day = day.minusDays(1)) {
      result.add(Map.of("date", day.toString(), "value", balance));
      while (
        idx < histories.size() && histories.get(idx).getCreatedAt().toLocalDate().isEqual(day)
      ) {
        balance -= histories.get(idx).getAmount();
        idx++;
      }
    }
    Collections.reverse(result);
    return result;
  }

  /**
   * 重新计算用户的积分总数
   * 通过累加所有积分历史记录来重新计算用户的当前积分
   */
  public int recalculateUserPoints(User user) {
    // 获取用户所有的积分历史记录（由于@Where注解，已删除的记录会被自动过滤）
    List<PointHistory> histories = pointHistoryRepository.findByUserOrderByIdAsc(user);

    int totalPoints = 0;
    for (PointHistory history : histories) {
      totalPoints += history.getAmount();
      // 重新计算每条历史记录的余额
      history.setBalance(totalPoints);
    }

    // 批量更新历史记录及用户积分
    pointHistoryRepository.saveAll(histories);
    user.setPoint(totalPoints);
    userRepository.save(user);

    return totalPoints;
  }

  /**
   * 重新计算用户的积分总数（通过用户名）
   */
  public int recalculateUserPoints(String userName) {
    User user = userRepository.findByUsername(userName).orElseThrow();
    return recalculateUserPoints(user);
  }

  @Transactional
  public DonationResponse donateToPost(String donorName, Long postId, int amount) {
    if (amount <= 0) {
      throw new FieldException("amount", "打赏积分必须大于0");
    }
    User donor = userRepository.findByUsername(donorName).orElseThrow();
    Post post = postRepository.findById(postId).orElseThrow();
    User author = post.getAuthor();
    if (author.getId().equals(donor.getId())) {
      throw new FieldException("post", "不能给自己打赏");
    }
    if (donor.getPoint() < amount) {
      throw new FieldException("point", "积分不足");
    }
    addPoint(donor, -amount, PointHistoryType.DONATE_SENT, post, null, author);
    addPoint(author, amount, PointHistoryType.DONATE_RECEIVED, post, null, donor);
    notificationService.createNotification(
      author,
      NotificationType.DONATION,
      post,
      null,
      null,
      donor,
      null,
      String.valueOf(amount)
    );
    postChangeLogService.recordDonation(post, donor, amount);
    DonationResponse response = buildDonationResponse(post);
    response.setBalance(donor.getPoint());
    return response;
  }

  public DonationResponse getPostDonations(Long postId) {
    Post post = postRepository.findById(postId).orElseThrow();
    return buildDonationResponse(post);
  }

  private DonationResponse buildDonationResponse(Post post) {
    List<PointHistory> histories =
      pointHistoryRepository.findTop10ByPostAndTypeOrderByCreatedAtDesc(
        post,
        PointHistoryType.DONATE_RECEIVED
      );
    List<DonationDto> donations = histories
      .stream()
      .collect(Collectors.collectingAndThen(Collectors.toMap(
          history -> {
            User donor = history.getFromUser();
            if (donor != null && donor.getId() != null) {
              return "user:" + donor.getId();
            }
            return "history:" + history.getId();
          },
          history -> {
            DonationDto dto = new DonationDto();
            User donor = history.getFromUser();
            if (donor != null) {
              dto.setUserId(donor.getId());
              dto.setUsername(donor.getUsername());
              dto.setAvatar(donor.getAvatar());
            }
            dto.setAmount(history.getAmount());
            dto.setCreatedAt(history.getCreatedAt());
            return dto;
          },
          (left, right) -> {
            left.setAmount(left.getAmount() + right.getAmount());
            if (
              left.getCreatedAt() == null ||
              (right.getCreatedAt() != null && right.getCreatedAt().isAfter(left.getCreatedAt()))
            ) {
              left.setCreatedAt(right.getCreatedAt());
            }
            return left;
          },
          java.util.LinkedHashMap::new
        ), map -> new java.util.ArrayList<>(map.values())));
    Long total = pointHistoryRepository.sumAmountByPostAndType(
      post,
      PointHistoryType.DONATE_RECEIVED
    );
    int safeTotal = 0;
    if (total != null) {
      safeTotal = total > Integer.MAX_VALUE ? Integer.MAX_VALUE : total.intValue();
    }
    DonationResponse response = new DonationResponse();
    response.setDonations(donations);
    response.setTotalAmount(safeTotal);
    return response;
  }
}
