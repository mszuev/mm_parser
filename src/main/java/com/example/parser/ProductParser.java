package com.example.parser;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class ProductParser {

    private static final Logger logger = LoggerFactory.getLogger(ProductParser.class);
    private WebDriver driver;
    private ProductLoader productLoader;
    private final String itemsUrl;

    public ProductParser(WebDriver driver, String itemsUrl) {
        this.driver = driver;
        this.itemsUrl = itemsUrl;
        this.productLoader = new ProductLoader(driver);
    }

    public void parseProducts(double targetCashbackPercentage, CashbackParser cashbackParser) {
        long startTime = System.currentTimeMillis();
        logger.info("Начало парсинга товаров по адресу: {}", itemsUrl);

        List<Product> productList = new ArrayList<>();
        double maxCashbackPercentage = 0.0;
        int currentPage = 1;

        while (true) {
            String pageUrl = (currentPage == 1) ? itemsUrl : itemsUrl + "page-" + currentPage;
            logger.info("Обработка страницы: {}", pageUrl);

            // Проверяем, загружена ли страница с товарами в наличии
            if (!productLoader.loadPage(pageUrl)) {
                break; // Выходим из цикла, если товары не найдены
            }

            productLoader.waitForProductsToLoad();

            List<WebElement> productElements = productLoader.getAllProducts();
            logger.info("Найдено товаров на странице: {}", productElements.size());

            for (WebElement productElement : productElements) {
                try {
                    Product product = new Product(productElement);
                    if (product.getCashbackPercentage() > maxCashbackPercentage) {
                        maxCashbackPercentage = product.getCashbackPercentage();
                    }

                    if (product.getCashbackPercentage() >= targetCashbackPercentage) {
                        productList.add(product);
                        cashbackParser.addProductToCsv(product);
                        logger.info("Добавлен товар: {}", product.getName());
                    }
                } catch (Exception e) {
                    logger.error("Ошибка при парсинге товара: {}. URL товара: {}",
                            e.getMessage(), driver.getCurrentUrl(), e);
                }
            }

            if (isOutOfStock()) {
                logger.info("На странице {} меньше 30 товаров. Прекращаем парсинг.", currentPage);
                break;
            }

            if (!productLoader.hasNextPage()) {
                break;
            }

            currentPage++;
        }

        if (productList.isEmpty()) {
            String message = String.format(
                    "Товары с заданным кэшбеком %.2f%% не найдены. " +
                            "Максимальный кэшбек в данной категории: %.2f%%",
                    targetCashbackPercentage, maxCashbackPercentage
            );
            logger.info(message);
        }

        logger.info("Парсинг завершен. Всего найдено товаров: {}", productList.size());
        long endTime = System.currentTimeMillis();
        long executionTimeInMillis = endTime - startTime;
        long seconds = (executionTimeInMillis / 1000) % 60;
        long minutes = (executionTimeInMillis / (1000 * 60)) % 60;
        long hours = (executionTimeInMillis / (1000 * 60 * 60)) % 24;

        logger.info("Время выполнения парсинга: {} ч {} мин {} сек", hours, minutes, seconds);
    }

    private boolean isOutOfStock() {
        try {
            // Проверяем количество товаров на странице
            List<WebElement> productElements = driver.findElements(By.cssSelector(ProductLoader.PRODUCT_ITEM_SELECTOR));
            return productElements.size() < 30; // Меньше 30 товаров

        } catch (NoSuchElementException e) {
            return false;
        }
    }
}