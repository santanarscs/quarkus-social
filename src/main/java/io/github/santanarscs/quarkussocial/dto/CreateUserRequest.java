package io.github.santanarscs.quarkussocial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
  @NotBlank(message = "Name is requried")
  private String name;
  @NotNull(message = "Age is required")
  private Integer age;

}
