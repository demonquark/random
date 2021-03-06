package edu.krishna.scrapers;


/*
  Scrapes the Business list of the Yelp data for categories
  Created by Krishna on 14-3-2017.
 */

import java.io.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YelpBusinessJsonScraper {

    public static void run(String[] args) {

        // Specify the location of the file
        String outputFileLocation = "data\\htmlOutput.html";
        outputToHTMLFile(getCategories(args),outputFileLocation);
        System.out.println("HTML saved to: " + outputFileLocation);

    }

    public static TreeMap <String, Integer> getCategories(String[] args){

        // Specify the location of the file
        String inputFileLocation = "data\\yelp_academic_dataset_business.json";
        String outputFileLocation = "data\\htmlOutput.html";
        TreeMap<String, Integer> categories = new TreeMap<String, Integer>();
        JSONArray objCategories;

        // Read the file locations from the arguments
        if(args != null && args.length > 0 && args[0] != null && args[0].length() > 0){
            inputFileLocation = args[0];
        }
        if(args != null && args.length > 1 && args[1] != null && args[1].length() > 0){
            outputFileLocation = args[1];
        }
        System.out.println("Reading from JSON (" + inputFileLocation + ")");

        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(inputFileLocation));

            // IMPORTANT: Assume every line is json Object
            while ((line = br.readLine()) != null) {
                try {
                    // Parse the line
                    objCategories = new JSONObject(line).optJSONArray("categories");
                    if (objCategories != null) {
                        // Add the categories to our list
                        for(Object category : objCategories){
                            String key = category.toString().trim();
                            if(categories.containsKey(key)){
                                categories.put(key,categories.get(key) + 1);
                            } else {
                                categories.put(key,1);
                            }
                        }
                    } else {
                        System.out.println("NULL CATEGORIES");
                    }
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

        System.out.println("Total number of categories: " + categories.size());
        return categories;

    }


    private static void outputToHTMLFile(TreeMap <String, Integer> categories, String fileLocation){

        BufferedWriter output = null;
        int numOfColumns = 3;

        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLocation), "utf-8"));
            int size = categories.size();
            int i = 0;

            // Write every line in the list
            for(Map.Entry<String, Integer> entry : categories.entrySet()){
                // Start a new list
                if(i % (size/numOfColumns) == 0 && (size - i > numOfColumns)){
                    output.write("<div class=\"col col-4\"><ul class=\"item-list\">");
                    output.newLine();
                }

                // Output the list item
                output.write("<li><a href=\"<php? if(group == A || C || E)echo 'step3a/' else 'step3b'; ?>'" + entry.getKey().trim().replaceAll(" +", "_").replaceAll("[^A-Za-z0-9_]", "-") + "; ?>'"
                        + "')\">" + entry.getKey() + "</a><span>" + entry.getValue() + " entries</span></li>");
                output.newLine();
                i++;

                // End the list
                if((i % (size/numOfColumns) == 0 && (size - i > numOfColumns)) || i == size){
                    output.write("</ul></div>");
                    output.newLine();
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
    }
}
