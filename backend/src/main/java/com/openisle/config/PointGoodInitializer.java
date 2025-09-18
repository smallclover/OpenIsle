package com.openisle.config;

import com.openisle.model.PointGood;
import com.openisle.repository.PointGoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Initialize default point mall goods. */
@Component
@RequiredArgsConstructor
public class PointGoodInitializer implements CommandLineRunner {

  private final PointGoodRepository pointGoodRepository;

  @Override
  public void run(String... args) {
    if (pointGoodRepository.count() == 0) {
      PointGood g1 = new PointGood();
      g1.setName("GPT Plus 1 个月");
      g1.setCost(20000);
      g1.setImage(
        "https://openisle-1307107697.cos.ap-guangzhou.myqcloud.com/assert/icons/chatgpt.png"
      );
      pointGoodRepository.save(g1);

      PointGood g2 = new PointGood();
      g2.setName("奶茶");
      g2.setCost(5000);
      g2.setImage(
        "https://openisle-1307107697.cos.ap-guangzhou.myqcloud.com/assert/icons/coffee.png"
      );
      pointGoodRepository.save(g2);
    }
  }
}
