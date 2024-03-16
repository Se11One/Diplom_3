package tests;

import client.User;
import client.UserClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import page_object.LoginPage;
import page_object.MainPage;
import page_object.RegisterPage;
import util.WebDriverUtil;

import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.*;

@RunWith(Parameterized.class)
public class RegistrationTest {

    private WebDriver driver;
    private String driverType;
    private User user;
    public static String accessToken;

    String name = randomAlphanumeric(4, 8);
    String email = randomAlphanumeric(6, 10) + "@yandex.ru";
    String password = randomAlphanumeric(10, 20);
    String passwordFailed = randomAlphanumeric(0, 5);

    public RegistrationTest(String driverType) {
        this.driverType = driverType;
    }

    @Before
    public void startUp() {
        driver = WebDriverUtil.initializeDriver(driverType);
        WebDriverUtil.navigateToUrl(driver, "https://stellarburgers.nomoreparties.site/");
    }

    @Parameterized.Parameters(name = "Результаты проверок браузера: {0}")
    public static Object[][] getDataDriver() {
        return new Object[][]{
                {"chromedriver"},
                {"yandexdriver"},
        };
    }

    @Test
    @DisplayName("Успешная регистрация.")
    @Description("Проверка успешной регистрации.")
    public void successfulRegistrationTest() {
        // Создание пользователя через интерфейс
        user = new User(name, email, password);

        MainPage mainPage = new MainPage(driver);
        mainPage.clickOnLoginButton();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickOnRegister();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.waitForLoadRegisterPage();
        registerPage.registration(user.getName(), user.getEmail(), user.getPassword());
        loginPage.waitForLoadEntrance();

        // Получение и сохранение токена через API метод для авторизации
        accessToken = UserClient.checkRequestAuthLogin(user);

        // Вывод токена в консоль
        System.out.println("Access Token: " + accessToken);
    }

    @Test
    @DisplayName("Неуспешная регистрация пользователя.")
    @Description("Проверяем неуспешную регистрацию пользователя при вводе пароля меньше 6 символов, и появление сообщения 'Некорректный пароль'.")
    public void failedPasswordRegistrationTest() {
        // Создание нового пользователя
        user = new User(name, email, password);

        MainPage mainPage = new MainPage(driver);
        mainPage.clickOnLoginButton();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickOnRegister();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.waitForLoadRegisterPage();
        registerPage.registration(user.getName(), user.getEmail(), passwordFailed);
        //Проверка появление текста "Некорректный пароль"
        Assert.assertTrue("Текст об ошибке отсутствует", driver.findElement(registerPage.errorPasswordText).isDisplayed());
    }

    @After
    public void tearDown() {
        // Закрытие браузера
        driver.quit();

        if (accessToken != null) {
            UserClient.deleteUser(accessToken);
        } else {
            System.out.println("AccessToken is null, cannot delete user.");
        }
    }

}