package client;

import io.qameta.allure.Step;
import io.restassured.internal.RestAssuredResponseOptionsGroovyImpl;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import org.hamcrest.Matchers;

import java.util.Locale;

public class UserClient {


@Step("Успешное создание уникального пользователя.")
public static String postCreateNewUser(User user) {
    Response response = given()
            .log().all()
            .header("Content-type", "application/json")
            .body(user)
            .when()
            //.post("/api/auth/register");
            .post("https://stellarburgers.nomoreparties.site/api/auth/register");

    if (response.getStatusCode() == 200) {
        String accessToken = response.jsonPath().getString("accessToken");
        return accessToken;
    } else {
        System.out.println("Ошибка: Регистрация не удалась");
        return null;
    }
}

    @Step("Неуспешный ответ сервера на регистрацию пользователя.")
    public void checkFailedResponseAuthRegister(Response response) {
        response.then().log().all()
                .assertThat().statusCode(403).and().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("Email, password and name are required fields"));
    }

    @Step("Логин под существующим пользователем.")
    public static String checkRequestAuthLogin(User user) {
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                //.post("/api/auth/login");
                .post("https://stellarburgers.nomoreparties.site/api/auth/login");

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("accessToken");
            return accessToken;
        } else {
            System.out.println("Ошибка: Вход не выполнен");
            return null;
        }
    }

    @Step("Логин с неверным логином и паролем.")
    public void checkFailedResponseAuthLogin(Response response) {
        response.then().log().all()
                .assertThat().statusCode(401).and().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("email or password are incorrect"));
    }

    @Step("Изменение данных пользователя с авторизацией.")
    public Response sendPatchRequestWithAuthorizationApiAuthUser(User user, String token) {
        return given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .header("authorization", token)
                .body(user)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Изменение данных пользователя без авторизации.")
    public Response sendPatchRequestWithoutAuthorizationApiAuthUser(User user) {
        return given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Успешный ответ сервера на изменение данных пользователя.")
    public void checkSuccessResponseAuthUser(Response response, String email, String name) {
        response.then().log().all()
                .assertThat()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .and().body("user.email", Matchers.is(email.toLowerCase(Locale.ROOT)))
                .and().body("user.name", Matchers.is(name));
    }

    @Step("Неуспешный ответ сервера на изменение данных пользователя.")
    public void checkFailedResponseAuthUser(Response response) {
        response.then().log().all()
                .assertThat().statusCode(401).and().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("You should be authorised"));
    }

    @Step("Удаление пользователя и возврат токена")
    public static String deleteUser(String accessToken){
        Response response = given()
                .header("Authorization", accessToken)
                .when()
                .delete("https://stellarburgers.nomoreparties.site/api/auth/user");

        if (response.getStatusCode() == 200) {
            System.out.println("Пользователь успешно удален");
            return accessToken;
        } else {
            System.out.println("Ошибка при удалении пользователя");
            return null;
        }
    }
}
