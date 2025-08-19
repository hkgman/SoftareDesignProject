package com.utility.company;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UiTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private String savedEmail;
    private String savedPassword;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public String generatePhoneNumber() {
        Random rand = new Random();

        int areaCode = rand.nextInt(100, 1000);
        int firstPart = rand.nextInt(100, 1000);
        int secondPart = rand.nextInt(10, 100);
        int thirdPart = rand.nextInt(10, 100);

        return "+7(" + areaCode + ")" + firstPart + "-" + secondPart + "-" + thirdPart;
    }

    public void register(String testEmail,String testFullName, String testPhone, String testBirthday, String testPassword)
    {
        driver.get("http://localhost:8080/login");

        // Ожидаем появления ссылки для регистрации
        click(By.linkText("Зарегистрироваться"));
        wait.until(ExpectedConditions.titleContains("Регистрация"));

        // Шаг 2: Заполнить форму регистрации
        sendKeys(By.name("email"), testEmail);
        sendKeys(By.name("fullName"), testFullName);
        sendKeys(By.name("phone"), testPhone);
        sendKeys(By.name("birthday"), testBirthday);
        sendKeys(By.name("password"), testPassword);
        sendKeys(By.name("passwordConfirm"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // Ожидаемый результат: Переход на страницу входа
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login"));
        Assertions.assertEquals("http://localhost:8080/login", driver.getCurrentUrl());
    }
    @Test
    @Order(1)
    public void testUserRegistrationAndLogin() {
        String uniqueId = UUID.randomUUID().toString();
        String testEmail = "testuser" + uniqueId + "@example.com";
        String testPhone = generatePhoneNumber();
        String testPassword = "securePassword1";
        String testFullName = "Тестовый Пользователь";
        String testBirthday = "2000-01-01";

        savedEmail = testEmail;  // Сохраняем email
        savedPassword = testPassword;  // Сохраняем пароль

        register(testEmail,testFullName,testPhone,testBirthday,testPassword);

        // Шаг 3: Войти с данными, использованными при регистрации
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // Ожидаемый результат: Переход на главную страницу с текстом Добро пожаловать
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        // Шаг 4: Переход на страницу профиль
        click(By.linkText("Профиль"));

        // Ожидаемый результат: Страница профиля с текстом "Данные профиля"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String profileTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Данные профиля", profileTitle);

        // Шаг 5: Проверка почты, ФИО и номера
        String profileEmail = getAttribute(By.name("email"), "value");
        String profileFullName = getAttribute(By.name("fullName"), "value");
        String profilePhone = getAttribute(By.name("phone"), "value");

        Assertions.assertEquals(testEmail, profileEmail, "Почта в профиле не совпадает с ожидаемой");
        Assertions.assertEquals(testFullName, profileFullName, "ФИО в профиле не совпадает с ожидаемым");
        Assertions.assertEquals(testPhone, profilePhone, "Номер телефона в профиле не совпадает с ожидаемым");

        // Шаг 6: Переход на страницу техники
        click(By.linkText("Техника"));

        // Ожидаемый результат: Страница с текстом "Список оборудования" и пустой списком
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentTitle = driver.findElement(By.tagName("h2")).getText();
        String equipmentListText = driver.findElement(By.tagName("p")).getText();

        Assertions.assertEquals("Список оборудования", equipmentTitle, "Заголовок страницы не совпадает");
        Assertions.assertTrue(equipmentListText.contains("Список вашей техники пуст"), "Текст о пустом списке не отображается");

        // Шаг 7: Нажать на кнопку "Выход"
        click(By.linkText("Выход"));

        // Ожидаемый результат: Страница входа открывается
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login?logout"));
        Assertions.assertEquals("http://localhost:8080/login?logout", driver.getCurrentUrl());
    }

    @Test
    @Order(2)
    public void testUserProfileUpdate() {
        String uniqueId = UUID.randomUUID().toString();
        String testEmail = "testuser" + uniqueId + "@example.com";
        String testPassword = "securePassword1";

        testEmail = savedEmail;
        testPassword = savedPassword;
        String updatedEmail = "updateduser" + uniqueId + "@example.com";
        String updatedPhone = generatePhoneNumber();
        String updatedFullName = "Обновленный Пользователь";
        String updatedPassword = "newSecurePassword1";

        // Шаг 1: Войти с данными, использованными при регистрации
        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // Ожидаемый результат: Переход на главную страницу с текстом Добро пожаловать
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        // Шаг 2: Нажать на элемент шапки "Профиль"
        click(By.linkText("Профиль"));

        // Ожидаемый результат: Пользователь находится на странице профиль с текстом в теге <h4> "Данные профиля"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String profileTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Данные профиля", profileTitle);

        // Шаг 3: Обновить данные в форме
        clearAndSendKeys(By.name("email"), updatedEmail);
        clearAndSendKeys(By.name("fullName"), updatedFullName);
        clearAndSendKeys(By.name("phone"), updatedPhone);
        clearAndSendKeys(By.name("password"), updatedPassword);
        click(By.cssSelector("button[type='submit']"));

        // Ожидаемый результат: Оставаться на странице, данные обновлены
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String updatedProfileTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Данные профиля", updatedProfileTitle);

        // Шаг 4: Нажать на элемент шапки "Выход"
        click(By.linkText("Выход"));

        // Ожидаемый результат: Страница входа открывается
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login?logout"));
        Assertions.assertEquals("http://localhost:8080/login?logout", driver.getCurrentUrl());

        // Шаг 5: Войти с обновленными данными (новая почта и новый пароль)
        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), updatedEmail);
        sendKeys(By.name("password"), updatedPassword);
        click(By.cssSelector("button[type='submit']"));
        savedEmail = updatedEmail;  // Сохраняем email
        savedPassword = updatedPassword;  // Сохраняем пароль
        // Ожидаемый результат: Переход на главную страницу с текстом Добро пожаловать
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String updatedWelcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", updatedWelcomeText);

        // Шаг 6: Нажать на элемент шапки "Профиль"
        click(By.linkText("Профиль"));

        // Ожидаемый результат: Страница профиля с обновленными данными
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String finalProfileTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Данные профиля", finalProfileTitle);

        // Шаг 7: Проверить, что данные в полях формы совпадают с обновленными значениями
        String finalEmail = getAttribute(By.name("email"), "value");
        String finalFullName = getAttribute(By.name("fullName"), "value");
        String finalPhone = getAttribute(By.name("phone"), "value");

        Assertions.assertEquals(updatedEmail, finalEmail, "Почта в профиле не совпадает с обновленной");
        Assertions.assertEquals(updatedFullName, finalFullName, "ФИО в профиле не совпадает с обновленным");
        Assertions.assertEquals(updatedPhone, finalPhone, "Номер телефона в профиле не совпадает с обновленным");
    }
    @Test
    @Order(3)
    public void addTypeForEquipment()
    {
        String testEmail = "sorokin.zxcv@gmail.com";
        String testPassword = "testPassword1";
        String testPhone = generatePhoneNumber();
        String testFullName = "Тестовый Пользователь";
        String testBirthday = "2000-01-01";


        register(testEmail,testFullName,testPhone,testBirthday,testPassword);

        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // Ожидаемый результат: Переход на главную страницу с текстом Добро пожаловать
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        click(By.linkText("Тип техники"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentPageTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Типы техники", equipmentPageTitle);
        WebElement addButton = driver.findElement(By.cssSelector("a.btn-primary"));
        Assertions.assertTrue(addButton.isDisplayed(), "Кнопка 'Добавить' не отображается");

        addButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String addEquipmentPageTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Данные типа техники", addEquipmentPageTitle);
        String typeText = "Тип техники 1";
        sendKeys(By.name("text"), typeText); // Изменено на корректное имя поля
        click(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentListTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Типы техники", equipmentListTitle);

        List<WebElement> types = driver.findElements(By.cssSelector(".card"));

        Assertions.assertTrue(types.size() > 0, "Карточка с добавленным типом не найдена.");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement text = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//strong[contains(text(), 'Тип техники 1')]")
            ));
            Assertions.assertNotNull(text, "Элемент с текстом 'Тип техники 1' найден."); // Успешная проверка
        } catch (TimeoutException e) {
            Assertions.fail("Элемент с текстом 'Тип техники 1' не найден."); // Ошибка, если элемент не найден
        }
    }

    @Test
    @Order(4)
    public void testAddEquipment() {
        String uniqueId = UUID.randomUUID().toString();
        String testEmail = "testuser" + uniqueId + "@example.com";
        String testPassword = "securePassword1";

        String equipmentName = "Тестовое оборудование";
        testEmail = savedEmail;
        testPassword = savedPassword;
        // Шаг 1: Войти по тестовым данным
        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // ОР 1: Пользователь находится на главной странице с текстом <h1> “Добро пожаловать”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        // Шаг 2: Нажать на элемент шапки "Техника"
        click(By.linkText("Техника"));

        // ОР 2: Пользователь находится на странице техники с текстом “Список оборудования”. На странице есть кнопка “Добавить”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentPageTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentPageTitle);
        WebElement addButton = driver.findElement(By.cssSelector("a.btn-primary"));
        Assertions.assertTrue(addButton.isDisplayed(), "Кнопка 'Добавить' не отображается");

        // Шаг 3: Нажать на кнопку "Добавить"
        addButton.click();

        // ОР 3: Пользователь находится на странице добавления техники с текстом <h4> “Редактирование техники”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String addEquipmentPageTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Редактирование техники", addEquipmentPageTitle);

        // Шаг 4: Ввести тестовые данные и сохранить
        sendKeys(By.name("name"), equipmentName); // Изменено на корректное имя поля
        WebElement typeSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("counterType")));
        Select select = new Select(typeSelect);
        select.selectByIndex(0); // Выбираем первый элемент из списка
        String selectedTypeText = select.getFirstSelectedOption().getText(); // Получаем текст первого элемента
        click(By.cssSelector("button[type='submit']"));

        // ОР 4: Переход на страницу списка техники с текстом “Список оборудования”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentListTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentListTitle);

        // Ищем все карточки по CSS-селектору
        List<WebElement> equipmentCards = driver.findElements(By.cssSelector(".card.mb-4.shadow-sm"));

        // Проверяем, что карточек больше 0
        Assertions.assertTrue(equipmentCards.size() > 0, "Карточка с добавленным оборудованием не найдена.");

    }

    @Test
    @Order(5)
    public void testUpdateEquipment() {
        String uniqueId = UUID.randomUUID().toString();
        String testEmail = "testuser" + uniqueId + "@example.com";
        String testPassword = "securePassword1";
        String equipmentName = "Обновленное оборудование";
        testEmail = savedEmail;
        testPassword = savedPassword;
        // Шаг 1: Войти по тестовым данным
        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // ОР 1: Пользователь находится на главной странице с текстом <h1> “Добро пожаловать”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        // Шаг 2: Нажать на элемент шапки "Техника"
        click(By.linkText("Техника"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentPageTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentPageTitle);

        WebElement editButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(., 'Изменить')]/i")));

        editButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        String addEquipmentPageTitle = driver.findElement(By.tagName("h4")).getText();
        Assertions.assertEquals("Редактирование техники", addEquipmentPageTitle);

        // Шаг 4: Ввести тестовые данные и сохранить
        clearAndSendKeys(By.name("name"), equipmentName); // Изменено на корректное имя поля
        WebElement typeSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("counterType")));
        Select select = new Select(typeSelect);
        select.selectByIndex(0); // Выбираем первый элемент из списка
        click(By.cssSelector("button[type='submit']"));

        // ОР 4: Переход на страницу списка техники с текстом “Список оборудования”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentListTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentListTitle);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement text = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h5[contains(text(), 'Обновленное оборудование')]")
            ));
            Assertions.assertNotNull(text, "Элемент с текстом 'Обновленное оборудование' найден."); // Успешная проверка
        } catch (TimeoutException e) {
            Assertions.fail("Элемент с текстом 'Обновленное оборудование' не найден."); // Ошибка, если элемент не найден
        }
    }

    @Test
    @Order(6)
    public void deleteEquipment()
    {
        String testEmail;
        String testPassword;
        testEmail = savedEmail;
        testPassword = savedPassword;
        driver.get("http://localhost:8080/login");
        sendKeys(By.name("username"), testEmail);
        sendKeys(By.name("password"), testPassword);
        click(By.cssSelector("button[type='submit']"));

        // ОР 1: Пользователь находится на главной странице с текстом <h1> “Добро пожаловать”
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String welcomeText = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertEquals("Добро пожаловать", welcomeText);

        // Шаг 2: Нажать на элемент шапки "Техника"
        click(By.linkText("Техника"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String equipmentPageTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentPageTitle);

        WebElement deleteButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(., 'Удалить')]/i")));

        deleteButton.click();

        Alert alert = driver.switchTo().alert();

        alert.accept();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        equipmentPageTitle = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertEquals("Список оборудования", equipmentPageTitle);


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement text = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//p[contains(text(), 'Список вашей техники пуст. Добавьте технику на ремонт!')]")
            ));
            Assertions.assertNotNull(text, "Элемент с такиим текстом найден."); // Успешная проверка
        } catch (TimeoutException e) {
            Assertions.fail("Элемент с таким текстом не найден."); // Ошибка, если элемент не найден
        }
    }


    // Утилитные методы для удобства
    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void sendKeys(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
    }
    private void clearAndSendKeys(By by, String value) {
        WebElement element = driver.findElement(by);
        element.clear();  // Очистить поле
        element.sendKeys(value);  // Ввести новое значение
    }
    private String getAttribute(By locator, String attribute) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getAttribute(attribute);
    }
}
