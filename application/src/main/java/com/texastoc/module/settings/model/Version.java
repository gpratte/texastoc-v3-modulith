package com.texastoc.module.settings.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Version {
  @Id
  private int id;
  private String version;
}
