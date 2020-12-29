package com.texastoc.module.player.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  public enum Type {ADMIN, USER}

  @Id
  private int id;
  @NotNull
  private Type type;
}
