package io.github.santanarscs.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.santanarscs.quarkussocial.domain.model.User;
import io.github.santanarscs.quarkussocial.dto.CreateUserRequest;
import io.github.santanarscs.quarkussocial.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserResouceTest {

  @Inject
  Validator validator;

  @TestHTTPResource("/users")
  URL apiUrl;

  @Test
  @DisplayName("should be create an user")
  @Order(1)
  public void testCreateUserSuccess() {
    CreateUserRequest request = new CreateUserRequest();
    request.setAge(30);
    request.setName("John Doe");

    var response = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(apiUrl)
        .then()
        .extract()
        .response();

    assertEquals(201, response.statusCode());
    User createdUser = response.getBody().as(User.class);
    assertEquals("John Doe", createdUser.getName());
    assertEquals(30, createdUser.getAge());
  }

  @Test
  @DisplayName("should return erro in create an user")
  @Order(2)
  public void testCreateUserError() {
    CreateUserRequest request = new CreateUserRequest();
    request.setAge(null);
    request.setName(null);
    var response = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(apiUrl)
        .then()
        .extract()
        .response();

    assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
    assertEquals("Validation Error", response.jsonPath().getString("message"));
    List<Map<String, String>> errors = response.jsonPath().getList("errors");
    assertNotNull(errors.get(0).get("message"));

  }

  @Test
  @DisplayName("should be list all users")
  @Order(3)
  public void getAllUsers() {
    given().contentType(ContentType.JSON)
    .when()
      .get(apiUrl)
    .then()
      .statusCode(200)
      .body("size()", Matchers.is(1));
  }
}
