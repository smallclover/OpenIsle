package com.openisle.model;

import com.openisle.search.SearchEntityListener;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tags")
@EntityListeners(SearchEntityListener.class)
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column
  private String icon;

  @Column
  private String smallIcon;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(nullable = false)
  private boolean approved = true;

  @CreationTimestamp
  @Column(
    nullable = false,
    updatable = false,
    columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)"
  )
  private LocalDateTime createdAt;

  // 改用redis缓存之后选择立即加载策略
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "creator_id")
  private User creator;
}
