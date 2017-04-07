package edu.krishna.scrapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * Created by Krishna on 7-4-2017.
 * Reads the yelp json files and writes the SQL INSERT lines for the tables
 */
public class SQLBuilder {

    public static void run(String[] args){

        // Write enumeration lists. These keep track of the Yelp provided ids. (The line number is the corresponding new database ID)
//        writeList("data\\yelp_academic_dataset_business.json", "data\\list-business.txt", "business_id");
//        writeList("data\\yelp_academic_dataset_user.json", "data\\list-user.txt", "user_id");
//        writeList(YelpBusinessJsonScraper.getCategories(args), "data\\list-categories.txt");
//        writeList("data\\yelp_academic_dataset_review.json", "data\\list-review.txt", "review_id");

        // Write to SQL
//        writeSQL("data\\yelp_academic_dataset_business.json", "data\\SQL-business.txt",
//                new String [] {"business_id", "name", "full_address", "score"});
//        writeSQL("data\\yelp_academic_dataset_user.json", "data\\SQL-user.txt",
//                new String [] {"user_id", "name"});
//        writeSQL(YelpBusinessJsonScraper.getCategories(args), "data\\SQL-categories.txt");
        writeSQLRelationshipTable("data\\yelp_academic_dataset_business.json", "data\\SQL-business-categories-rel.txt",
                "data\\list-business.txt", "data\\list-categories.txt", "business_id");

    }

    public static void writeSQLRelationshipTable(String inputFileLocation, String outputFileLocation, String list1FileLocation, String list2FileLocation, String id_key){

        System.out.println("Writing SQL (" + outputFileLocation + ") from JSON (" + inputFileLocation + ")");

        JSONObject item;
        StringBuilder sBuilder = new StringBuilder();
        int counter = 0;
        int businessID = 0;
        int categoryID = 0;
        JSONArray objCategories;
        BufferedReader br = null;
        BufferedWriter output = null;

        try {
            String line;
            br = new BufferedReader(new FileReader(inputFileLocation));
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                try {
                    // Parse the line
                    item = new JSONObject(line);
                    businessID = readIDfromList(list1FileLocation, item.optString(id_key));

                    objCategories = item.optJSONArray("categories");
                    if (objCategories != null) {
                        // Add the categories to our list
                        for(Object category : objCategories){
                            // get the values
                            counter++;
                            categoryID = readIDfromList(list2FileLocation, category.toString().trim());

                            // Add the INSERT values
                            if(categoryID >0 && businessID > 0){
                                sBuilder.setLength(0);
                                sBuilder.append('(').append(counter);
                                sBuilder.append(',').append(' ').append(businessID);
                                sBuilder.append(',').append(' ').append(categoryID);
                                sBuilder.append(')').append(',');
                                output.write(sBuilder.toString());
                                output.newLine();

                            } else {
                                System.out.println("ERROR: Unknown ID - busID=" + businessID +" (" + item.optString(id_key) + ") | catID=" + categoryID + " (" + category.toString() +")");
                            }

                            if(counter % 1000 == 0){
                                System.out.print("." + ((counter/1000)%10));
                                if(counter % 100000 == 0){
                                    System.out.println();
                                }
                            }

                        }
                    }

                } catch (JSONException ex) {
                    System.out.println("INVALID JSON OBJECT.");
                    System.out.println(ex.toString());
                } catch (NoSuchElementException ex){
                    System.out.println("NO ELEMENT");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();
        System.out.println("SQL saved to: " + outputFileLocation);
    }

    public static void writeSQL(TreeMap<String, Integer> categories, String outputFileLocation){

        System.out.println("Writing SQL (" + outputFileLocation + ") from category map");

        StringBuilder sBuilder = new StringBuilder();
        int counter = 0;
        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // Write every line in the list
            for(Map.Entry<String, Integer> entry : categories.entrySet()){

                // Move to the next line if there are lines before this one
                if(counter > 0){
                    output.write(',');
                    output.newLine();
                }

                // Write a INSERT line
                counter++;
                sBuilder.setLength(0);
                sBuilder.append('(').append(counter);
                sBuilder.append(',').append(' ').append('\'').append(SQLInjectionEscaper.escapeString(entry.getKey(), false)).append('\'');
                sBuilder.append(')');
                output.write(sBuilder.toString());

                if(counter % 1000 == 0){
                    System.out.print("." + ((counter/1000)%10));
                    if(counter % 100000 == 0){
                        System.out.println();
                    }
                }
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();
        System.out.println("SQL saved to: " + outputFileLocation);
    }

    public static void writeSQL(String inputFileLocation, String outputFileLocation, String [] id_keys){

        System.out.println("Writing SQL (" + outputFileLocation + ") from JSON (" + inputFileLocation + ")");

        JSONObject item;
        StringBuilder sBuilder = new StringBuilder();
        int counter = 0;
        String value;
        BufferedReader br = null;
        BufferedWriter output = null;

        try {
            String line;
            br = new BufferedReader(new FileReader(inputFileLocation));
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                try {
                    // Parse the line
                    item = new JSONObject(line);
                    if (item != null) {

                        // Move to the next line if there are lines before this one
                        if(counter > 0){
                            output.write(',');
                            output.newLine();
                        }

                        // Start a INSERT line (add the PRIMARY ID)
                        counter++;
                        sBuilder.setLength(0);
                        sBuilder.append('(').append(counter);

                        // Add the INSERT values
                        for(String id : id_keys){
                            value = item.optString(id);
                            if (item.has(id)) {
                                sBuilder.append(',').append(' ').append('\'').append(SQLInjectionEscaper.escapeString(value, false)).append('\'');
                            } else {
                                sBuilder.append(',').append(' ').append("NULL");
                            }
                        }

                        // Finish the INSERT line
                        sBuilder.append(')');
                        output.write(sBuilder.toString());

                        if(counter % 1000 == 0){
                            System.out.print("." + ((counter/1000)%10));
                            if(counter % 100000 == 0){
                                System.out.println();
                            }
                        }

                    } else {
                        System.out.println("BUSINESS_ID NOT FOUND");
                    }
                } catch (JSONException ex) {
                    System.out.println("INVALID JSON OBJECT.");
                    System.out.println(ex.toString());
                } catch (NoSuchElementException ex){
                    System.out.println("NO ELEMENT");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();
        System.out.println("SQL saved to: " + outputFileLocation);
    }

    public static void writeList(TreeMap<String, Integer> categories, String outputFileLocation){

        System.out.println("Writing list (" + outputFileLocation + ") from category map");

        int counter = 0;
        String id;
        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // Write every line in the list
            for(Map.Entry<String, Integer> entry : categories.entrySet()){
                counter++;
                if(counter % 1000 == 0){
                    System.out.print("." + ((counter/1000)%10));
                    if(counter % 100000 == 0){
                        System.out.println();
                    }
                }

                output.write(entry.getKey());
                output.newLine();
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();
        System.out.println("List saved to: " + outputFileLocation);
    }

    public static void writeList(String inputFileLocation, String outputFileLocation, String id_key){

        System.out.println("Writing list (" + outputFileLocation + ") from JSON (" + inputFileLocation + ")");

        String id;
        int counter = 0;
        BufferedReader br = null;
        BufferedWriter output = null;

        try {
            String line;
            br = new BufferedReader(new FileReader(inputFileLocation));
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                counter++;
                if(counter % 1000 == 0){
                    System.out.print("." + ((counter/1000)%10));
                    if(counter % 100000 == 0){
                        System.out.println();
                    }
                }

                try {
                    // Parse the line
                    id = new JSONObject(line).getString(id_key);
                    if (id != null && id.length() > 0) {
                        output.write(id);
                        output.newLine();
                    } else {
                        System.out.println("BUSINESS_ID NOT FOUND");
                    }
                } catch (JSONException ex) {
                    System.out.println("INVALID JSON OBJECT.");
                } catch (NoSuchElementException ex){
                    System.out.println("NO ELEMENT");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();
        System.out.println("List saved to: " + outputFileLocation);
    }

    private static int readIDfromList(String listFileLocation, String searchString) {

        int counter = 0;
        BufferedReader br = null;
        boolean stringFound = false;
        try {
            String line;
            br = new BufferedReader(new FileReader(listFileLocation));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null && !stringFound) {
                try {
                    // Increment counter till we find the string  TODO: write a better search algorithm(?)
                    counter++;
                    stringFound = line.equals(searchString);

                } catch (JSONException ex) {
                    System.out.println("INVALID JSON OBJECT.");
                } catch (NoSuchElementException ex){
                    System.out.println("NO CATEGORIES");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return stringFound ? counter : -1;
    }

}
