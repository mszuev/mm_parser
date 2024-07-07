package com.example.parser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication
public class CashbackParserApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CashbackParserApplication.class);

    @Value("${parser.items.url}")
    private String itemsUrl;

    @Value("${parser.target.cashback.percentage}")
    private double targetCashbackPercentage;

    @Value("${parser.chrome.profile.path}")
    private String chromeProfilePath;

    @Value("${parser.csv.folder.path}")
    private String csvFolderPath;

    public static void main(String[] args) {
        SpringApplication.run(CashbackParserApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Установка системного свойства для драйвера
        System.setProperty("webdriver.chrome.driver", "src/main/chromedriver/chromedriver.exe");

        // Настройки Chrome с указанием пути к профилю
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + chromeProfilePath);
        options.addArguments("blink-settings=imagesEnabled=false"); // отключение изображений

        // Создание экземпляра драйвера с указанными настройками
        WebDriver driver = new ChromeDriver(options);

        // Создание объекта парсера
        ProductParser parser = new ProductParser(driver, itemsUrl);

        // Инициализация CSV
        CashbackParser cashbackParser = new CashbackParser(itemsUrl, csvFolderPath);
        try {
            cashbackParser.startCsv();
        } catch (IOException e) {
            logger.error("Ошибка при инициализации CSV: {}", e.getMessage(), e);
            return;
        }

        // Парсинг товаров с заданным процентом кэшбека
        parser.parseProducts(targetCashbackPercentage, cashbackParser);

        try {
            cashbackParser.finishCsv();
        } catch (IOException e) {
            logger.error("Ошибка при закрытии CSV файла: {}", e.getMessage(), e);
        }

        // Закрытие браузера
        driver.quit();
    }
}