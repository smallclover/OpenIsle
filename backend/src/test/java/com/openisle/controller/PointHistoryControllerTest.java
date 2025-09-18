package com.openisle.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openisle.config.CustomAccessDeniedHandler;
import com.openisle.config.SecurityConfig;
import com.openisle.mapper.PointHistoryMapper;
import com.openisle.model.Role;
import com.openisle.model.User;
import com.openisle.repository.UserRepository;
import com.openisle.service.JwtService;
import com.openisle.service.PointService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointHistoryController.class)
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, CustomAccessDeniedHandler.class })
class PointHistoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private JwtService jwtService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private PointService pointService;

  @MockBean
  private PointHistoryMapper pointHistoryMapper;

  @Test
  void trendReturnsSeries() throws Exception {
    Mockito.when(jwtService.validateAndGetSubject("token")).thenReturn("user");
    User user = new User();
    user.setUsername("user");
    user.setPassword("p");
    user.setEmail("u@example.com");
    user.setRole(Role.USER);
    Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
    List<Map<String, Object>> data = List.of(
      Map.of("date", java.time.LocalDate.now().minusDays(1).toString(), "value", 100),
      Map.of("date", java.time.LocalDate.now().toString(), "value", 110)
    );
    Mockito.when(pointService.trend(Mockito.eq("user"), Mockito.anyInt())).thenReturn(data);

    mockMvc
      .perform(
        get("/api/point-histories/trend").param("days", "2").header("Authorization", "Bearer token")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].value").value(100))
      .andExpect(jsonPath("$[1].value").value(110));
  }
}
