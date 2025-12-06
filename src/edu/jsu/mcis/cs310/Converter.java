package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Converter {

    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {

        String result = "{}";

        try {

            // Read all CSV rows
            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                return result;
            }

            // First row = header
            String[] headers = allRows.get(0);

            JsonObject root = new JsonObject();

            // Build ColHeadings array
            JsonArray colHeadings = new JsonArray();
            for (String h : headers) {
                colHeadings.add(h);
            }
            root.put("ColHeadings", colHeadings);

            JsonArray prodNums = new JsonArray();
            JsonArray data = new JsonArray();

            // Data rows start from index 1
            for (int r = 1; r < allRows.size(); r++) {

                String[] row = allRows.get(r);

                // ProdNum is always first column
                prodNums.add(row[0]);

                JsonArray rowArr = new JsonArray();

                // Remaining columns
                for (int c = 1; c < row.length; c++) {

                    String header = headers[c];
                    String value = row[c];

                    if ("Season".equals(header) || "Episode".equals(header)) {
                        // store these as integers in JSON
                        rowArr.add(Integer.parseInt(value));
                    }
                    else {
                        rowArr.add(value);
                    }
                }

                data.add(rowArr);
            }

            root.put("ProdNums", prodNums);
            root.put("Data", data);

            result = Jsoner.serialize(root);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result.trim();
    }

    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {

        String result = "";

        try {

            // Parse JSON into an object
            JsonObject obj = Jsoner.deserialize(jsonString, new JsonObject());

            JsonArray colHeadings = (JsonArray) obj.get("ColHeadings");
            JsonArray prodNums = (JsonArray) obj.get("ProdNums");
            JsonArray data = (JsonArray) obj.get("Data");

            // Prepare writer
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(
                    stringWriter,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    "\n" // force \n line endings
            );

            // Header row
            String[] headerRow = new String[colHeadings.size()];
            for (int i = 0; i < colHeadings.size(); i++) {
                headerRow[i] = colHeadings.get(i).toString();
            }
            csvWriter.writeNext(headerRow);

            // Data rows
            for (int i = 0; i < prodNums.size(); i++) {

                List<String> row = new ArrayList<>();

                // First column: ProdNum
                row.add(prodNums.get(i).toString());

                JsonArray rowData = (JsonArray) data.get(i);

                for (int j = 0; j < rowData.size(); j++) {

                    Object value = rowData.get(j);

                    // header index is offset by 1 (ProdNum is column 0)
                    String headerName = headerRow[j + 1];

                    // Episode must be formatted with leading zero when < 10
                    if ("Episode".equals(headerName) && value instanceof Number) {
                        int ep = ((Number) value).intValue();
                        if (ep < 10) {
                            row.add(String.format("%02d", ep)); // 01, 02, 03, ...
                        }
                        else {
                            row.add(Integer.toString(ep));
                        }
                    }
                    else {
                        row.add(value.toString());
                    }
                }

                csvWriter.writeNext(row.toArray(new String[0]));
            }

            csvWriter.close();

            // Normalize line endings and remove final newline
            result = stringWriter.toString().replace("\r\n", "\n");
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result.trim();
    }

}
