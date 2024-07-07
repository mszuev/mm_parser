package com.example.parser;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.List;

public class ProductLoader {
    private static final Logger logger = LoggerFactory.getLogger(ProductLoader.class);
    private WebDriver driver;
    private WebDriverWait wait;
    public static final String PRODUCT_ITEM_SELECTOR =
        "[data-test='product-item']:not(.catalog-item-mobile_out-of-stock):not(.catalog-item-regular-desktop_out-of-stock)";

    public ProductLoader(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public boolean hasNextPage() {
        try {
            WebElement nextPageLink = driver.findElement(By.cssSelector("a[rel='next']"));
            return nextPageLink != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean loadPage(String url) {
        driver.get(url);
        try {
            // Пытаемся найти элемент с заданным селектором
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(PRODUCT_ITEM_SELECTOR)));
            logger.info("Загружена страница: {}", url);
            return true; // Страница загружена успешно
        } catch (TimeoutException e) {
            // Если элемент не найден за заданное время, то страница не содержит нужных товаров
            logger.info("На странице {} не найдены товары в наличии. Прекращаем парсинг.", url);
            return false; // Страница не загружена
        }
    }

    public List<WebElement> getAllProducts() {
        WebElement productListContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("catalog-items-list")));
        return productListContainer.findElements(By.cssSelector(PRODUCT_ITEM_SELECTOR));
    }

    public void waitForProductsToLoad() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(PRODUCT_ITEM_SELECTOR)));
    }
}