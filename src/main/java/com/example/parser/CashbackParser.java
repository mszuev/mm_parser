package com.example.parser;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileWriter;
import java.io.IOException;

public class CashbackParser {

    private static final Logger logger = LoggerFactory.getLogger(CashbackParser.class);
    private String itemsUrl;
    private String csvFolderPath;
    private CSVWriter fileWriter;

    public CashbackParser(String itemsUrl, String csvFolderPath) {
        this.itemsUrl = itemsUrl;
        this.csvFolderPath = csvFolderPath;
    }

    public void startCsv() throws IOException {
        String[] parts = itemsUrl.split("/");
        String csvFileName = parts[parts.length - 1].isEmpty() ? parts[parts.length - 2] : parts[parts.length - 1] + ".csv";
        String csvFolderPathAndFileName = csvFolderPath + csvFileName;
        fileWriter = new CSVWriter(new FileWriter(csvFolderPathAndFileName),
                '\t', // Разделитель табуляция
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        fileWriter.writeNext(new String[]{"Название", "Ссылка", "Цена", "Кэшбек", "Процент кэшбека"});
        logger.info("Создан CSV файл: {}", csvFolderPathAndFileName);
    }

    public void addProductToCsv(Product product) {
        fileWriter.writeNext(new String[]{
                product.getName(),
                product.getLink(),
                String.valueOf((int) product.getPrice()), // Преобразование в int для нормальной сортировки в csv
                String.valueOf((int) product.getCashbackAmount()),
                String.format("%.2f%%", product.getCashbackPercentage())
        });
        logger.info("Товар добавлен в CSV файл: {}", product.getName());
    }

    public void finishCsv() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
            logger.info("CSV файл успешно записан.");
        }
    }
}