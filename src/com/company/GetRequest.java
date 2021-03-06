package com.company;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetRequest {

    public static void main(String[] args) throws IOException {

        Socket s = new Socket("me.utm.md", 80 );

        PrintWriter wtr = new PrintWriter(s.getOutputStream());

        //Prints the request string to the output stream
        wtr.println("GET / HTTP/1.1");
        wtr.println("Host: me.utm.md");
        wtr.println("Connection: keep-alive");
        wtr.println("Accept-Language: ro,en");
        wtr.println("DNT: 1");
        wtr.println("Save-Data: <sd-token>");
        wtr.println("");
        wtr.flush();

        //Creates a BufferedReader that contains the server response
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String outStr;
        String content = "";

        //Prints each line of the response
        while((outStr = bufRead.readLine()) != null){
            content += outStr;
        }

       // System.out.println(content);
        //Closes out buffer and writer
        bufRead.close();
        wtr.close();

        Pattern pattern = Pattern.compile("[^\"']*\\.(?:png|jpg|gif)");
        List<String> allPhotos = new ArrayList<>();

        Matcher m = pattern.matcher(content.toString());
        while (m.find()) {
            allPhotos.add(m.group());
        }

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

        List<String> allPaths = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("([^\\/]+$)");
        File downloads = new File("D://Programare in Retea//SocketHTTP//img//");

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        final Semaphore semaphore = new Semaphore(2);

        for (String link : allPhotosLinks) {

            ImageDownloader imageDownloader = null;

                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    imageDownloader = new ImageDownloader(link, downloads);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            executor.execute(imageDownloader);
                semaphore.release();
            }
            System.out.println("Maximum threads inside pool " + executor.getMaximumPoolSize());
            executor.shutdown();
        }

}
