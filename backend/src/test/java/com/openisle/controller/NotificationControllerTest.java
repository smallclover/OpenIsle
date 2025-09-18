package com.openisle.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.openisle.dto.NotificationDto;
import com.openisle.dto.PostSummaryDto;
import com.openisle.mapper.NotificationMapper;
import com.openisle.model.Notification;
import com.openisle.model.NotificationType;
import com.openisle.model.Post;
import com.openisle.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NotificationService notificationService;

  @MockBean
  private NotificationMapper notificationMapper;

  @Test
  void listNotifications() throws Exception {
    Notification n = new Notification();
    n.setId(1L);
    n.setType(NotificationType.POST_VIEWED);
    Post p = new Post();
    p.setId(2L);
    n.setPost(p);
    n.setCreatedAt(LocalDateTime.now());
    when(notificationService.listNotifications("alice", null, 0, 30)).thenReturn(List.of(n));

    NotificationDto dto = new NotificationDto();
    dto.setId(1L);
    PostSummaryDto ps = new PostSummaryDto();
    ps.setId(2L);
    dto.setPost(ps);
    when(notificationMapper.toDto(n)).thenReturn(dto);

    mockMvc
      .perform(
        get("/api/notifications").principal(new UsernamePasswordAuthenticationToken("alice", "p"))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].post.id").value(2));
  }

  @Test
  void listUnreadNotifications() throws Exception {
    Notification n = new Notification();
    n.setId(5L);
    n.setType(NotificationType.POST_VIEWED);
    when(notificationService.listNotifications("alice", false, 0, 30)).thenReturn(List.of(n));

    NotificationDto dto = new NotificationDto();
    dto.setId(5L);
    when(notificationMapper.toDto(n)).thenReturn(dto);

    mockMvc
      .perform(
        get("/api/notifications/unread").principal(
          new UsernamePasswordAuthenticationToken("alice", "p")
        )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(5));
  }

  @Test
  void markReadEndpoint() throws Exception {
    mockMvc
      .perform(
        post("/api/notifications/read")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"ids\":[1,2]}")
          .principal(new UsernamePasswordAuthenticationToken("alice", "p"))
      )
      .andExpect(status().isOk());

    verify(notificationService).markRead("alice", List.of(1L, 2L));
  }

  @Test
  void unreadCountEndpoint() throws Exception {
    when(notificationService.countUnread("alice")).thenReturn(3L);

    mockMvc
      .perform(
        get("/api/notifications/unread-count").principal(
          new UsernamePasswordAuthenticationToken("alice", "p")
        )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.count").value(3));
  }
}
