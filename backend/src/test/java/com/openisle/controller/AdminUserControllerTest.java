package com.openisle.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openisle.model.Notification;
import com.openisle.model.NotificationType;
import com.openisle.model.User;
import com.openisle.repository.NotificationRepository;
import com.openisle.repository.UserRepository;
import com.openisle.service.EmailSender;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NotificationRepository notificationRepository;

  @MockBean
  private EmailSender emailSender;

  @Test
  void approveMarksNotificationsRead() throws Exception {
    User u = new User();
    u.setId(1L);
    u.setEmail("a@a.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    Notification n = new Notification();
    n.setId(2L);
    n.setRead(false);
    when(
      notificationRepository.findByTypeAndFromUser(NotificationType.REGISTER_REQUEST, u)
    ).thenReturn(List.of(n));

    mockMvc.perform(post("/api/admin/users/1/approve")).andExpect(status().isOk());

    assertTrue(n.isRead());
    verify(notificationRepository).saveAll(List.of(n));
  }

  @Test
  void rejectMarksNotificationsRead() throws Exception {
    User u = new User();
    u.setId(1L);
    u.setEmail("a@a.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    Notification n = new Notification();
    n.setId(2L);
    n.setRead(false);
    when(
      notificationRepository.findByTypeAndFromUser(NotificationType.REGISTER_REQUEST, u)
    ).thenReturn(List.of(n));

    mockMvc.perform(post("/api/admin/users/1/reject")).andExpect(status().isOk());

    assertTrue(n.isRead());
    verify(notificationRepository).saveAll(List.of(n));
  }
}
