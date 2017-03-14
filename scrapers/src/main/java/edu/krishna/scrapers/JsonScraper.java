package edu.krishna.scrapers;


/**
 * Created by Krishna on 14-3-2017.
 */

import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonScraper {
    public static void main(String[] args) {

        System.out.println("Hello World");

        File reader = new File("abc.txt");
        System.out.println(reader.getAbsolutePath());


//        String jsonData = "";
//        BufferedReader br = null;
//        try {
//            String line;
//            br = new BufferedReader(new FileReader("/Users/<username>/Documents/Crunchify_JSON.txt"));
//            while ((line = br.readLine()) != null) {
//                jsonData += line + "\n";
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (br != null)
//                    br.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        // System.out.println("File Content: \n" + jsonData);
//        JSONObject obj = new JSONObject(jsonData);
//        System.out.println("blogURL: " + obj.getString("blogURL"));
//        System.out.println("twitter: " + obj.getString("twitter"));
//        System.out.println("social: " + obj.getJSONObject("social"));
    }
}
