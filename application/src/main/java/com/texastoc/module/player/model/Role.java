package com.texastoc.module.player.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Role {

  public enum Type {ADMIN, USER}

  @Id
  private int id;
  private Type type;
}
