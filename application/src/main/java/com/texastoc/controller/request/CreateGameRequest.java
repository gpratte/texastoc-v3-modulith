package com.texastoc.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGameRequest {
  @NotNull(message = "host id is required")
  private Integer hostId;

  @NotNull(message = "date is required")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  @NotNull(message = "transportRequired is required")
  private Boolean transportRequired;

}
