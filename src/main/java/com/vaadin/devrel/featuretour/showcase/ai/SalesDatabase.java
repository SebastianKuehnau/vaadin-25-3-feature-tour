package com.vaadin.devrel.featuretour.showcase.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates a small sales database for the AI data explorer, and a read-only
 * database user that the LLM-written queries run as.
 */
@Component
public class SalesDatabase implements ApplicationRunner {

    static final String READER_USER = "ai_reader";
    static final String READER_PASSWORD = "ai_reader";

    static final String SCHEMA = """
            Tables:
            sales(id INT, order_date DATE, region VARCHAR, country VARCHAR, product VARCHAR,
                  category VARCHAR, units INT, revenue DECIMAL(12,2))
            Column notes: revenue is in EUR. region is one of EMEA, Americas, APAC.
            category is one of Subscriptions, Services, Training.
            Data covers January 2025 until today.
            Dialect: H2 (use FORMATDATETIME(order_date, 'yyyy-MM') for months, YEAR() and QUARTER() work).
            """;

    private final JdbcTemplate jdbc;
    private final String url;

    SalesDatabase(JdbcTemplate jdbc, @Value("${spring.datasource.url}") String url) {
        this.jdbc = jdbc;
        this.url = url;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INT PRIMARY KEY,
                    order_date DATE NOT NULL,
                    region VARCHAR(20) NOT NULL,
                    country VARCHAR(40) NOT NULL,
                    product VARCHAR(60) NOT NULL,
                    category VARCHAR(20) NOT NULL,
                    units INT NOT NULL,
                    revenue DECIMAL(12, 2) NOT NULL)
                """);
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sales", Integer.class);
        if (count != null && count == 0) {
            seed();
        }
        jdbc.execute("CREATE USER IF NOT EXISTS " + READER_USER + " PASSWORD '" + READER_PASSWORD + "'");
        jdbc.execute("GRANT SELECT ON sales TO " + READER_USER);
    }

    private void seed() {
        record Product(String name, String category, int price) {
        }
        List<Product> products = List.of(
                new Product("Vaadin Core Support", "Subscriptions", 1200),
                new Product("Vaadin Prime", "Subscriptions", 3600),
                new Product("Vaadin Ultimate", "Subscriptions", 9800),
                new Product("Modernization Workshop", "Services", 6400),
                new Product("Architecture Review", "Services", 4800),
                new Product("Vaadin Flow Training", "Training", 1900),
                new Product("Vaadin AI Training", "Training", 2300));
        String[][] countries = {
                {"EMEA", "Finland"}, {"EMEA", "Germany"}, {"EMEA", "United Kingdom"}, {"EMEA", "Spain"},
                {"Americas", "United States"}, {"Americas", "Canada"}, {"Americas", "Brazil"},
                {"APAC", "Japan"}, {"APAC", "Australia"}, {"APAC", "India"}};

        var random = new Random(253);
        LocalDate start = LocalDate.of(2025, 1, 1);
        long days = LocalDate.now().toEpochDay() - start.toEpochDay();
        List<Object[]> rows = new ArrayList<>();
        for (int id = 1; id <= 1500; id++) {
            // Growth over time: later dates are more likely
            LocalDate date = start.plusDays((long) (days * Math.sqrt(random.nextDouble())));
            Product product = products.get(random.nextInt(products.size()));
            String[] country = countries[random.nextInt(countries.length)];
            int units = 1 + random.nextInt(product.category().equals("Training") ? 12 : 4);
            BigDecimal revenue = BigDecimal.valueOf(product.price() * units * (0.85 + random.nextDouble() * 0.3))
                    .setScale(2, RoundingMode.HALF_UP);
            rows.add(new Object[]{id, Date.valueOf(date), country[0], country[1], product.name(),
                    product.category(), units, revenue});
        }
        jdbc.batchUpdate("INSERT INTO sales VALUES (?, ?, ?, ?, ?, ?, ?, ?)", rows);
    }

    /**
     * A data source that connects as the read-only user.
     */
    DataSource readOnlyDataSource() {
        var dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        // Connection settings such as DB_CLOSE_DELAY need admin rights, so connect with the bare URL
        dataSource.setUrl(url.split(";")[0]);
        dataSource.setUsername(READER_USER);
        dataSource.setPassword(READER_PASSWORD);
        return dataSource;
    }
}
