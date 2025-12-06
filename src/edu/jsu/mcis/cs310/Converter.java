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

            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> rows = reader.readAll();

            String[] headerRow = rows.get(0);
            List<String[]> bodyRows = rows.subList(1, rows.size());

            JsonObject root = new JsonObject();

            /* --- Column Headings --- */
            JsonArray headings = new JsonArray();
            for (String h : headerRow) {
                headings.add(h);
            }
            root.put("ColHeadings", headings);

            /* --- ProdNums and Data Arrays --- */
            JsonArray prodNums = new JsonArray();
            JsonArray dataRows = new JsonArray();

            for (String[] row : bodyRows) {

                prodNums.add(row[0]);      // first column

                JsonArray rowData = new JsonArray();

                for (int c = 1; c < row.length; c++) {

                    String head = headerRow[c];
                    String value = row[c];

                    if ("Season".equals(head) || "Episode".equals(head)) {
                        rowData.add(Integer.parseInt(value));
                    }
                    else {
                        rowData.add(value);
                    }
                }

                dataRows.add(rowData);
            }

            root.put("ProdNums", prodNums);
            root.put("Data", dataRows);

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

            /* --- Parse JSON --- */
            JsonObject obj = (JsonObject) Jsoner.deserialize(jsonString);

            JsonArray headings = (JsonArray) obj.get("ColHeadings");
            JsonArray prodNums = (JsonArray) obj.get("ProdNums");
            JsonArray data = (JsonArray) obj.get("Data");

            /* --- Prepare CSV Writer --- */
            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw);

            /* --- Write Header --- */
            String[] headerRow = headings.toArray(new String[0]);
            writer.writeNext(headerRow);

            /* --- Write Data Rows --- */
            for (int i = 0; i < prodNums.size(); i++) {

                List<String> row = new ArrayList<>();

                row.add(prodNums.get(i).toString());

                JsonArray values = (JsonArray) data.get(i);

                for (int j = 0; j < values.size(); j++) {

                    Object val = values.get(j);
                    row.add(val.toString());
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
