package com.example.parser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

public class Product {
    private String name;
    private String link;
    private double price;
    private double cashbackAmount;
    private double cashbackPercentage;

    public Product(WebElement productElement) {
        this.name = productElement.findElement(By.cssSelector("a[data-test='product-name-link']")).getText(); //попробовать getAttribute("title");
        this.link = productElement.findElement(By.cssSelector("a[data-test='product-name-link']")).getAttribute("href");
        this.price = parsePrice(productElement.findElement(By.cssSelector("span[data-test='product-price'], div[data-test='product-price']")).getText());

        // Извлечение суммы кэшбека
        List<WebElement> cashbackAmountElements = productElement.findElements(By.cssSelector("span[data-test='bonus-amount']"));
        if (!cashbackAmountElements.isEmpty()) {
            String cashbackAmountText = cashbackAmountElements.get(0).getText();
            this.cashbackAmount = parsePrice(cashbackAmountText);
        } else {
            this.cashbackAmount = 0.0;
        }

        // Извлечение процента кэшбека (нужно для мобильной версии сайта, так как значение процента кэша отсутствует)
        List<WebElement> cashbackPercentageElements = productElement.findElements(By.cssSelector("span[data-test='bonus-percent']"));
        if (!cashbackPercentageElements.isEmpty()) {
            String cashbackPercentageText = cashbackPercentageElements.get(0).getText();
            this.cashbackPercentage = parsePercentage(cashbackPercentageText);
        } else {
            this.cashbackPercentage = (cashbackAmount / price) * 100;
        }
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public double getPrice() {
        return price;
    }

    public double getCashbackAmount() {
        return cashbackAmount;
    }

    public double getCashbackPercentage() {
        return cashbackPercentage;
    }

    private double parsePrice(String priceString) {
        if (priceString == null || priceString.trim().isEmpty()) {
            return 0.0;
        }
        String cleanedPrice = priceString.replaceAll("[\\s₽]", "").replace(",", ".");
        return Double.parseDouble(cleanedPrice);
    }

    private double parsePercentage(String percentageString) {
        if (percentageString == null || percentageString.trim().isEmpty()) {
            return 0.0;
        }
        String cleanedPercentage = percentageString.replaceAll("[\\s%]", "").replace(",", ".");
        return Double.parseDouble(cleanedPercentage);
    }
}