package com.example;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // CSV → JSON
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String csvFileName = "data.csv";

        List<Employee> csvList = parseCSV(columnMapping, csvFileName);
        if (csvList != null) {
            String jsonFromCsv = listToJson(csvList);
            writeString(jsonFromCsv, "data.json");
        }

        // XML → JSON
        String xmlFileName = "data.xml";
        List<Employee> xmlList = parseXML(xmlFileName);
        if (xmlList != null) {
            String jsonFromXml = listToJson(xmlList);
            writeString(jsonFromXml, "data2.json");
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            List<Employee> list = csv.parse();
            logger.info("CSV файл успешно прочитан: " + fileName);
            return list;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения CSV файла: " + fileName, e);
            return null;
        }
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            doc.getDocumentElement().normalize();

            Node root = doc.getDocumentElement(); // <staff>
            NodeList nodeList = root.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    long id = Long.parseLong(elem.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = elem.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = elem.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = elem.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(elem.getElementsByTagName("age").item(0).getTextContent());

                    Employee emp = new Employee(id, firstName, lastName, country, age);
                    list.add(emp);
                }
            }
            logger.info("XML файл успешно прочитан: " + fileName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при парсинге XML файла: " + fileName, e);
        }
        return list;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
            logger.info("JSON файл успешно создан: " + fileName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка записи JSON файла: " + fileName, e);
        }
    }
}