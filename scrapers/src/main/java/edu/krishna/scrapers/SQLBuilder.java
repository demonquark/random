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
        writeList("data\\yelp_academic_dataset_business.json", "data\\list-business.txt", "business_id");
        writeList("data\\yelp_academic_dataset_user.json", "data\\list-user.txt", "user_id");
        writeList(YelpBusinessJsonScraper.getCategories(args), "data\\list-categories.txt");
        writeList("data\\yelp_academic_dataset_review.json", "data\\list-review.txt", "review_id");

        // Write to SQL
        writeSQL("data\\yelp_academic_dataset_business.json", "data\\SQL-business.txt",
                new String [] {"business_id", "name", "full_address", "score"});
        writeSQL("data\\yelp_academic_dataset_user.json", "data\\SQL-user.txt",
                new String [] {"user_id", "name"});
        writeSQLReviews("data\\yelp_academic_dataset_review.json", "data\\SQL-review.txt",
                "data\\list-business.txt", "data\\list-user.txt", new String [] {"review_id", "text", "stars", "businessID", "userID"}, 6);
        writeSQL(YelpBusinessJsonScraper.getCategories(args), "data\\SQL-categories.txt");
        writeSQLRelationshipTable("data\\yelp_academic_dataset_business.json", "data\\SQL-business-categories-rel.txt",
                "data\\list-business.txt", "data\\list-categories.txt", "business_id");

    }


    public static void writeSQLReviews(String inputFileLocation, String outputFileLocation, String list1FileLocation, String list2FileLocation, String [] id_keys, int maxReviews){

        System.out.println("Writing SQL (" + outputFileLocation + ") from JSON (" + inputFileLocation + ")");

        JSONObject item;
        StringBuilder sBuilder = new StringBuilder();
        int counter = 0;
        boolean skipped = false;
        String value;
        BufferedReader br = null;
        BufferedWriter output = null;
        TreeMap<String, ListItem> businesses = readIDSfromList(list1FileLocation);
        TreeMap<String, ListItem> users = readIDSfromList(list2FileLocation);
        ListItem business = null;
        ListItem user = null;

        try {
            String line;
            br = new BufferedReader(new FileReader(inputFileLocation));
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileLocation), "utf-8"));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                try {
                    // Parse the line
                    item = new JSONObject(line);
                    business = null;
                    user = null;
                    if (item != null) {

                        // Move to the next line if there are lines before this one
                        if(counter > 0 && !skipped){
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
                                if(id.equals("stars")){
                                    sBuilder.append(',').append(' ').append(item.optInt("stars", 3));
                                } else {
                                    sBuilder.append(',').append(' ').append('\'').append(SQLInjectionEscaper.escapeString(value, false)).append('\'');
                                }
                            } else {
                                if (id.equals("businessID")) {
                                    // Update the businessID list accordingly
                                    business = businesses.get(item.optString("business_id"));
                                    if(business != null ) {
                                        business.count++;
                                        business.sum_score += item.optInt("stars", 3);
                                        businesses.put(value, business);
                                    }
                                    sBuilder.append(',').append(' ').append(business != null ? business.index : "NULL");
                                } else if (id.equals("userID")){
                                    user = users.get(item.optString("user_id"));
                                    sBuilder.append(',').append(' ').append(user != null ? user.index : "NULL");
                                }else {
                                    sBuilder.append(',').append(' ').append("NULL");
                                }
                            }
                        }

                        // Finish the INSERT line
                        sBuilder.append(')');

                        if(business != null && (maxReviews < 0 || business.count < maxReviews)){
                            output.write(sBuilder.toString());
                            skipped = false;
                        } else {
                            skipped = true;
                        }

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

    public static void writeSQLRelationshipTable(String inputFileLocation, String outputFileLocation, String list1FileLocation, String list2FileLocation, String id_key1){

        System.out.println("Writing SQL (" + outputFileLocation + ") from JSON (" + inputFileLocation + ")");

        JSONObject item;
        StringBuilder sBuilder = new StringBuilder();
        int counter = 0;
        TreeMap<String, ListItem> businesses = readIDSfromList(list1FileLocation);
        TreeMap<String, ListItem> categories = readIDSfromList(list2FileLocation);

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
                    ListItem temp = businesses.get(item.optString(id_key1));
                    businessID = temp != null ? temp.index : -1;

                    objCategories = item.optJSONArray("categories");
                    if (objCategories != null) {
                        // Add the categories to our list
                        for(Object category : objCategories){
                            // get the values
                            counter++;
                            temp = categories.get(category);
                            categoryID = temp != null ? temp.index : -1;

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
                                System.out.println("ERROR: Unknown ID - busID=" + businessID + " (" + item.optString(id_key1) + ") | catID=" + categoryID + " (" + category.toString() +")");
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

    private static TreeMap<String, ListItem> readIDSfromList(String listFileLocation){

        int counter = 0;
        TreeMap<String, ListItem> businesses = new TreeMap<String, ListItem>();

        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(listFileLocation));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                try {
                    // Increment counter till we find the string
                    counter++;
                    businesses.put(line, new ListItem(counter));

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

        System.out.println("Total number of list items (" + listFileLocation + "): " + businesses.size());

        return businesses;

    }


}
