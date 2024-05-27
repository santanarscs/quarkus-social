package io.github.santanarscs.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.santanarscs.quarkussocial.domain.model.Follower;
import io.github.santanarscs.quarkussocial.domain.model.Post;
import io.github.santanarscs.quarkussocial.domain.model.User;
import io.github.santanarscs.quarkussocial.domain.repository.FollowerRepository;
import io.github.santanarscs.quarkussocial.domain.repository.PostRepository;
import io.github.santanarscs.quarkussocial.domain.repository.UserRepository;
import io.github.santanarscs.quarkussocial.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
public class PostResourceTest {

  @Inject
  UserRepository userRepository;

  @Inject
  FollowerRepository followerRepository;

  @Inject
  PostRepository postRepository;

  Long userId;
  Long userFollowerId;
  Long userNotFollwerId;

  @BeforeEach
  @Transactional
  public void setup() {
    var user = new User();
    user.setAge(31);
    user.setName("Some user");
    userRepository.persist(user);
    userId = user.getId();

    Post post = new Post();
    post.setText("Hello world");
    post.setUser(user);
    postRepository.persist(post);


    var userNotFollower = new User();
    userNotFollower.setAge(31);
    userNotFollower.setName("Some user 2");
    userRepository.persist(userNotFollower);
    userNotFollwerId = userNotFollower.getId();

    var userFollwer = new User();
    userFollwer.setAge(31);
    userFollwer.setName("Some user 2");
    userRepository.persist(userFollwer);
    userFollowerId = userFollwer.getId();

    Follower follower = new Follower();
    follower.setUser(user);
    follower.setFollower(userFollwer);
    followerRepository.persist(follower);
    
  }

  @Test
  @DisplayName("should create a post for a user")
  public void createPostTest() {
    var postRequest = new CreatePostRequest();
    postRequest.setText("Some text");
    given()
      .contentType(ContentType.JSON)
      .body(postRequest)
      .pathParam("userId", userId)
    .when()
      .post()
    .then()
      .statusCode(201);
  }

  @Test
  @DisplayName("should return 404 when try to make a post for a inexistent user")
  public void notCreatePost() {
    var postRequest = new CreatePostRequest();
    postRequest.setText("Some text");
    given()
      .contentType(ContentType.JSON)
      .body(postRequest)
      .pathParam("userId", 999)
    .when()
      .post()
    .then()
      .statusCode(404);
  }

  @Test
  @DisplayName("should return 400 when followerId header isnt not present")
  public void listPOstUserFollowerHeaderNotSendTest(){
    given()
      .pathParam("userId", userId)
    .when()
      .get()
    .then()
      .statusCode(400)
      .body(Matchers.is("You forgot the header followerId"));
  }

  @Test
  @DisplayName("should return 404 when user doesnt exists")
  public void listPOstUserNotFoundTest(){
    var fakeUserId = 999;
    given()
      .pathParam("userId", fakeUserId)
    .when()
      .get()
    .then()
      .statusCode(404);
  }

  
  

  
  @Test
  @DisplayName("should return 400 when follower doesnt exists")
  public void listPOstFollowerNotFoundTest(){
    var fakeFollowerId = 999;
    given()
      .pathParam("userId", userId)
      .header("followerId", fakeFollowerId)
    .when()
      .get()
    .then()
      .statusCode(404)
      .body(Matchers.is("Inexistent Follower"));
  }

  @Test
  @DisplayName("should return 403 when follower doesnt follow")
  public void listPOstFollowerNotAFollowerTest(){
    given()
      .pathParam("userId", userId)
      .header("followerId", userNotFollwerId)
    .when()
      .get()
    .then()
      .statusCode(403)
      .body(Matchers.is("You can't see this posts"));
  }

  
  @Test
  @DisplayName("should return list")
  public void listPostTest(){
    given()
      .pathParam("userId", userId)
      .header("followerId", userFollowerId)
    .when()
      .get()
    .then()
      .statusCode(200)
      .body("size()", Matchers.is(1));
  }
}
