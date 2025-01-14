package io.github.santanarscs.quarkussocial.rest;

import java.time.LocalDateTime;

import io.github.santanarscs.quarkussocial.domain.model.Post;
import io.github.santanarscs.quarkussocial.domain.model.User;
import io.github.santanarscs.quarkussocial.domain.repository.FollowerRepository;
import io.github.santanarscs.quarkussocial.domain.repository.PostRepository;
import io.github.santanarscs.quarkussocial.domain.repository.UserRepository;
import io.github.santanarscs.quarkussocial.dto.CreatePostRequest;
import io.github.santanarscs.quarkussocial.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

  private UserRepository userRepository;
  private PostRepository repository;
  private FollowerRepository followerRepository;

  @Inject
  public PostResource(UserRepository userRepository, PostRepository repository, FollowerRepository followerRepository) {
    this.userRepository = userRepository;
    this.repository = repository;
    this.followerRepository = followerRepository;
  }

  @POST
  @Transactional
  public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request) {
    User user = userRepository.findById(userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Post post = new Post();
    post.setText(request.getText());
    post.setUser(user);

    repository.persist(post);

    return Response.status(Response.Status.CREATED).build();
  }

  @GET
  public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId) {
    User user = userRepository.findById(userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if(followerId == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("You forgot the header followerId").build();
    }
    
    User follower = userRepository.findById(followerId);
    if(follower == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("Inexistent Follower").build();
    }
    boolean follows = followerRepository.follows(follower, user);

    if(!follows) {
      return Response.status(Response.Status.FORBIDDEN).entity("You can't see this posts").build();
    }

    PanacheQuery<Post> query = repository.find("user",Sort.by("dateTime", Direction.Descending), user);
    var list = query.list().stream().map(PostResponse::fromEntity).toList();
    return Response.ok(list).build();
  }

}
