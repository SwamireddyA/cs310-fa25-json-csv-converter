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

        String output = "{}";

        try {

            // Read CSV into memory
            CSVReader csv = new CSVReader(new StringReader(csvString));
            List<String[]> rows = csv.readAll();

            // First row = column headings
            String[] headings = rows.get(0);

            JsonObject root = new JsonObject();

            // Build column heading array
            JsonArray headingArray = new JsonArray();
            for (String h : headings) {
                headingArray.add(h);
            }
            root.put("ColHeadings", headingArray);

            // ProdNums and Data arrays
            JsonArray prodNumArray = new JsonArray();
            JsonArray dataArray = new JsonArray();

            // Process data rows
            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                // Add ProdNum
                prodNumArray.add(row[0]);

                JsonArray rowValues = new JsonArray();

                // Start at column 1 because column 0 is ProdNum
                for (int c = 1; c < row.length; c++) {

                    String heading = headings[c];
                    String value = row[c];

                    // Convert integers where required
                    if ("Season".equals(heading) || "Episode".equals(heading)) {
                        rowValues.add(Integer.parseInt(value));
                    }
                    else {
                        rowValues.add(value);
                    }
                }

                dataArray.add(rowValues);
            }

            // Insert into root object
            root.put("ProdNums", prodNumArray);
            root.put("Data", dataArray);

            output = Jsoner.serialize(root);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return output.trim();
    }

    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {

        String output = "";

        try {

            // Parse JSON input
            JsonObject root = (JsonObject) Jsoner.deserialize(jsonString);

            JsonArray headings = (JsonArray) root.get("ColHeadings");
            JsonArray prodNums = (JsonArray) root.get("ProdNums");
            JsonArray data = (JsonArray) root.get("Data");

            // Prepare CSV writer
            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw);

            // Write heading row
            String[] headingRow = headings.toArray(new String[0]);
            writer.writeNext(headingRow);

            // Write each CSV row
            for (int i = 0; i < prodNums.size(); i++) {

                List<String> row = new ArrayList<>();

                // First column = ProdNum
                row.add(prodNums.get(i).toString());

                JsonArray dataRow = (JsonArray) data.get(i);

                for (Object cell : dataRow) {
                    row.add(cell.toString());
                }

                writer.writeNext(row.toArray(new String[0]));
            }

            writer.close();
            output = sw.toString().trim();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return output.trim();
    }
}
