package com.openisle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Item available in the point mall. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "point_goods")
public class PointGood {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int cost;

  private String image;
}
