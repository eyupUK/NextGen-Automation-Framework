package com.example.util;

import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CsvDataLoader {

    public static class CityRow {
        public final String query;
        public final String expectedCountry; // can be empty
        public final int forecastDays;

        public CityRow(String q, String c, int d) {
            this.query = q; this.expectedCountry = c; this.forecastDays = d;
        }
    }

    public static List<CityRow> load(String classpathCsv) {
        try (Reader r = new InputStreamReader(
                Objects.requireNonNull(CsvDataLoader.class.getResourceAsStream("/data/" + classpathCsv)))) {
            CSVReader reader = new CSVReader(r);
            List<CityRow> rows = new ArrayList<>();
            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                // Expect query,expectedCountry,forecastDays
                String query = row[0].trim();
                String expectedCountry = row[1].trim();
                int days = Integer.parseInt(row[2].trim());
                rows.add(new CityRow(query, expectedCountry, days));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV: " + classpathCsv, e);
        }
    }
}
