package com.openisle.config;

import com.openisle.model.MessageConversation;
import com.openisle.repository.MessageConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelInitializer implements CommandLineRunner {

  private final MessageConversationRepository conversationRepository;

  @Override
  public void run(String... args) {
    if (conversationRepository.countByChannelTrue() == 0) {
      MessageConversation chat = new MessageConversation();
      chat.setChannel(true);
      chat.setName("吹水群");
      chat.setDescription("吹水聊天");
      chat.setAvatar(
        "https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/32647273e2334d14adfd4a6ce9db0643.jpeg"
      );
      conversationRepository.save(chat);

      MessageConversation tech = new MessageConversation();
      tech.setChannel(true);
      tech.setName("技术讨论群");
      tech.setDescription("讨论技术相关话题");
      tech.setAvatar(
        "https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/5edde9a5864e471caa32491dbcdaa8b2.png"
      );
      conversationRepository.save(tech);
    }
  }
}
