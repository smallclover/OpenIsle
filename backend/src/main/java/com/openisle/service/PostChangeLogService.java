package com.openisle.service;

import com.openisle.model.*;
import com.openisle.repository.PostChangeLogRepository;
import com.openisle.repository.PostRepository;
import com.openisle.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostChangeLogService {

  private final PostChangeLogRepository logRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private User getSystemUser() {
    return userRepository
      .findByUsername("system")
      .orElseThrow(() -> new IllegalStateException("System user not found"));
  }

  public void recordContentChange(Post post, User user, String oldContent, String newContent) {
    PostContentChangeLog log = new PostContentChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.CONTENT);
    log.setOldContent(oldContent);
    log.setNewContent(newContent);
    logRepository.save(log);
  }

  public void recordTitleChange(Post post, User user, String oldTitle, String newTitle) {
    PostTitleChangeLog log = new PostTitleChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.TITLE);
    log.setOldTitle(oldTitle);
    log.setNewTitle(newTitle);
    logRepository.save(log);
  }

  public void recordCategoryChange(Post post, User user, String oldCategory, String newCategory) {
    PostCategoryChangeLog log = new PostCategoryChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.CATEGORY);
    log.setOldCategory(oldCategory);
    log.setNewCategory(newCategory);
    logRepository.save(log);
  }

  public void recordTagChange(Post post, User user, Set<Tag> oldTags, Set<Tag> newTags) {
    PostTagChangeLog log = new PostTagChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.TAG);
    log.setOldTags(oldTags.stream().map(Tag::getName).collect(Collectors.joining(",")));
    log.setNewTags(newTags.stream().map(Tag::getName).collect(Collectors.joining(",")));
    logRepository.save(log);
  }

  public void recordClosedChange(Post post, User user, boolean oldClosed, boolean newClosed) {
    PostClosedChangeLog log = new PostClosedChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.CLOSED);
    log.setOldClosed(oldClosed);
    log.setNewClosed(newClosed);
    logRepository.save(log);
  }

  public void recordPinnedChange(
    Post post,
    User user,
    java.time.LocalDateTime oldPinnedAt,
    java.time.LocalDateTime newPinnedAt
  ) {
    PostPinnedChangeLog log = new PostPinnedChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.PINNED);
    log.setOldPinnedAt(oldPinnedAt);
    log.setNewPinnedAt(newPinnedAt);
    logRepository.save(log);
  }

  public void recordFeaturedChange(Post post, User user, boolean oldFeatured, boolean newFeatured) {
    PostFeaturedChangeLog log = new PostFeaturedChangeLog();
    log.setPost(post);
    log.setUser(user);
    log.setType(PostChangeType.FEATURED);
    log.setOldFeatured(oldFeatured);
    log.setNewFeatured(newFeatured);
    logRepository.save(log);
  }

  public void recordVoteResult(Post post) {
    PostVoteResultChangeLog log = new PostVoteResultChangeLog();
    log.setPost(post);
    log.setUser(getSystemUser());
    log.setType(PostChangeType.VOTE_RESULT);
    logRepository.save(log);
  }

  public void recordLotteryResult(Post post) {
    PostLotteryResultChangeLog log = new PostLotteryResultChangeLog();
    log.setPost(post);
    log.setUser(getSystemUser());
    log.setType(PostChangeType.LOTTERY_RESULT);
    logRepository.save(log);
  }

  public void recordDonation(Post post, User donor, int amount) {
    PostDonateChangeLog log = new PostDonateChangeLog();
    log.setPost(post);
    log.setUser(donor);
    log.setType(PostChangeType.DONATE);
    log.setAmount(amount);
    logRepository.save(log);
  }

  public void deleteLogsForPost(Post post) {
    logRepository.deleteByPost(post);
  }

  public List<PostChangeLog> listLogs(Long postId) {
    Post post = postRepository
      .findById(postId)
      .orElseThrow(() -> new com.openisle.exception.NotFoundException("Post not found"));
    return logRepository.findByPostOrderByCreatedAtAsc(post);
  }
}
