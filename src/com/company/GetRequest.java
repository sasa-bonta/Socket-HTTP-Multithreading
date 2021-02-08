package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetRequest {

    public static void main(String[] args) throws IOException {

        URL url = new URL("http://me.utm.md/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Host", "me.utm.md:80");
        con.setRequestProperty("Content-Type", "text/html");
        con.setRequestProperty("Accept-Language", "ro, en");
        con.setRequestProperty("DNT", "1");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");

        int status = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();
//        System.out.println(content);

        Pattern pattern = Pattern.compile("[^\"']*\\.(?:png|jpg|gif)");
        List<String> allPhotos = new ArrayList<>();

        Matcher m = pattern.matcher(content.toString());
        while (m.find()) {
            allPhotos.add(m.group());
        }

//        allPhotos.forEach((photo) -> {
//            System.out.println(photo);
//        });

        List<String> allPhotosLinks = new ArrayList<>();

        allPhotos.forEach((photo) -> {
            if (photo.startsWith("http://")) {
//                System.out.println(photo);
                allPhotosLinks.add(photo);
            } else {
//                System.out.printf("http://me.utm.md/" + photo + "\n");
                allPhotosLinks.add("http://me.utm.md/" + photo);
            }
        });

        Pattern pattern2 = Pattern.compile("([^\\/]+$)");

//        allPhotosLinks.forEach((link) -> {
//            System.out.println(link);
//            Matcher fileName = pattern2.matcher(link);
//            while (fileName.find()) {
//                System.out.println(fileName.group());
//            }

            //http://mib.utm.md/files/news/EUROINVENT_2020/euroinvent.png
            //euroinvent.png

            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        for (String link : allPhotosLinks) {
            Matcher fileName = pattern2.matcher(link);
            DownloadThread downloadThread = null;
//            System.out.println(link);
            if (fileName.find()) {

//                System.out.println(fileName.group());
                try {
                    downloadThread = new DownloadThread(link, fileName.group());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                executor.execute(downloadThread);
            }
        }

        System.out.println("Maximum threads inside pool " + executor.getMaximumPoolSize());
        executor.shutdown();
    }
}
