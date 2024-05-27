package io.github.santanarscs.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.santanarscs.quarkussocial.domain.model.Follower;
import io.github.santanarscs.quarkussocial.domain.model.User;
import io.github.santanarscs.quarkussocial.domain.repository.FollowerRepository;
import io.github.santanarscs.quarkussocial.domain.repository.UserRepository;
import io.github.santanarscs.quarkussocial.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FollowerResourceTest {

  @Inject
  UserRepository userRepository;

  @Inject
  FollowerRepository followerRepository;

  Long userId;
  Long userFollowerId;

  @BeforeEach
  @Transactional
  public void setup() {
    var user = new User();
    user.setAge(31);
    user.setName("Some user");
    userRepository.persist(user);
    userId = user.getId();

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
  @DisplayName("should return 409 when followerId is same to userId")
  @Order(1)
  public void sameUserAsFollowerTest() {
    var body = new FollowerRequest();
    body.setFollowerId(userId);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .pathParam("userId", userId)
        .when()
        .put()
        .then()
        .statusCode(Response.Status.CONFLICT.getStatusCode())
        .body(Matchers.is("You can't follow yourself"));
  }

  @Test
  @DisplayName("should return 404 on follow user when userId doesnt exists")
  @Order(2)
  public void userNotFoundWhenTryToFollowTest() {
    var body = new FollowerRequest();
    body.setFollowerId(userId);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .pathParam("userId", 999)
        .when()
        .put()
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  @DisplayName("should follow a user")
  @Order(3)
  public void followUserTest() {
    var body = new FollowerRequest();
    body.setFollowerId(userFollowerId);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .pathParam("userId", userId)
        .when()
        .put()
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());
  }

  @Test
  @DisplayName("should return 404 on list user when userId doesnt exists")
  @Order(4)
  public void userNotFoundWhenListFollowersTest() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("userId", 999)
        .when()
        .get()
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  @DisplayName("should list followers")
  @Order(5)
  public void listFollowersTest() {
    var response = given()
        .contentType(ContentType.JSON)
        .pathParam("userId", userId)
        .when()
        .get()
        .then()
        .extract().response();
    var followersCount = response.jsonPath().get("followersCount");
    assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    assertEquals(1, followersCount);
  }

  @Test
  @DisplayName("should return 404 on unfllow user when userId doesnt exists")
  public void userNotFoundWhenUnfllowerUserTest() {
    given()
        .pathParam("userId", 999)
        .when()
        .delete()
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }
  @Test
  @DisplayName("should unfllow user")
  public void unfllowUserTest() {
    given()
        .pathParam("userId", userId)
        .queryParam("followerId", userFollowerId)
        .when()
        .delete()
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());
  }

}
