package com.openisle.service;

import static org.mockito.Mockito.*;

import com.openisle.model.*;
import com.openisle.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReactionServiceTest {

  @Test
  void reactToPostSendsEmailEveryFive() {
    ReactionRepository reactionRepo = mock(ReactionRepository.class);
    UserRepository userRepo = mock(UserRepository.class);
    PostRepository postRepo = mock(PostRepository.class);
    CommentRepository commentRepo = mock(CommentRepository.class);
    MessageRepository messageRepo = mock(MessageRepository.class);
    NotificationService notif = mock(NotificationService.class);
    EmailSender email = mock(EmailSender.class);
    ReactionService service = new ReactionService(
      reactionRepo,
      userRepo,
      postRepo,
      commentRepo,
      messageRepo,
      notif,
      email
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(1L);
    user.setUsername("bob");
    User author = new User();
    author.setId(2L);
    author.setEmail("a@a.com");
    Post post = new Post();
    post.setId(3L);
    post.setAuthor(author);

    when(userRepo.findByUsername("bob")).thenReturn(Optional.of(user));
    when(postRepo.findById(3L)).thenReturn(Optional.of(post));
    when(reactionRepo.findByUserAndPostAndType(user, post, ReactionType.LIKE)).thenReturn(
      Optional.empty()
    );
    when(reactionRepo.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));
    when(reactionRepo.countReceived(author.getUsername())).thenReturn(5L);

    service.reactToPost("bob", 3L, ReactionType.LIKE);

    verify(email).sendEmail("a@a.com", "你有新的互动", "https://ex.com/messages");
    verify(notif).sendCustomPush(author, "你有新的互动", "https://ex.com/messages");
  }
}
