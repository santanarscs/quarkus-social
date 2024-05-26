package io.github.santanarscs.quarkussocial.dto;

import java.util.List;

import lombok.Data;

@Data
public class FollowersUserResponse {
  private Integer followersCount;
  private List<FollowerResponse> content;
}
