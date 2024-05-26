package io.github.santanarscs.quarkussocial.rest;

import java.util.List;

import io.github.santanarscs.quarkussocial.domain.model.Follower;
import io.github.santanarscs.quarkussocial.domain.model.User;
import io.github.santanarscs.quarkussocial.domain.repository.FollowerRepository;
import io.github.santanarscs.quarkussocial.domain.repository.UserRepository;
import io.github.santanarscs.quarkussocial.dto.FollowerRequest;
import io.github.santanarscs.quarkussocial.dto.FollowerResponse;
import io.github.santanarscs.quarkussocial.dto.FollowersUserResponse;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users/{userId}/followers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FollowerResource {

  private FollowerRepository repository;
  private UserRepository userRepository;

  public FollowerResource(FollowerRepository repository, UserRepository userRepository) {
    this.repository = repository;
    this.userRepository = userRepository;
  }

  @PUT
  @Transactional
  public Response followUser(@PathParam("userId") Long userId, FollowerRequest request) {
    if(userId.equals(request.getFollowerId())) {
      return Response.status(Response.Status.CONFLICT).entity("You can't follow yourself").build();
    }
    User user = userRepository.findById(userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    User follower = userRepository.findById(request.getFollowerId());
    boolean follows = repository.follows(follower, user);
    
    if(!follows) {
      var entity = new Follower();
      entity.setUser(user);
      entity.setFollower(follower);
      repository.persist(entity);
    }

    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  public Response getFollowers(@PathParam("userId") Long userId) {
    User user = userRepository.findById(userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    List<Follower> list = repository.findByUser(userId);
    FollowersUserResponse responseObj = new FollowersUserResponse();
    responseObj.setFollowersCount(list.size());
    var followerList = list.stream().map(FollowerResponse::new).toList();
    responseObj.setContent(followerList);
    return Response.ok(responseObj).build();
  }

  @DELETE
  @Transactional
  public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {
    User user = userRepository.findById(userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    repository.deleteByFollowerAndUser(followerId, userId);

    return Response.status(Response.Status.NO_CONTENT).build();
  }

}
