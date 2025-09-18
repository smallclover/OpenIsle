package com.openisle.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "message_conversations")
public class MessageConversation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Indicates whether this conversation represents a public channel
  @Column(nullable = false)
  private boolean channel = false;

  // Channel metadata
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String avatar;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_message_id")
  private Message lastMessage;

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonBackReference
  private Set<MessageParticipant> participants = new HashSet<>();

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonBackReference
  private Set<Message> messages = new HashSet<>();
}
