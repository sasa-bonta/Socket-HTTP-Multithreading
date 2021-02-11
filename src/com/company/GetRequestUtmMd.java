package com.company;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetRequestUtmMd {

    public static void main(String[] args) throws IOException {

        SSLSocketFactory factory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket =
                (SSLSocket)factory.createSocket("www.verisign.com", 443);

        socket.startHandshake();

        PrintWriter wtr = new PrintWriter(socket.getOutputStream());

        //Prints the request string to the output stream
        wtr.println("GET / HTTP/1.1");
        wtr.println("Host: utm.md");
        wtr.println("");
        wtr.flush();

        //Creates a BufferedReader that contains the server response
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
//                System.out.printf("https://utm.md/" + photo + "\n");
                allPhotosLinks.add("https://utm.md/" + photo);
            }
        });

        List<String> allPaths = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("([^\\/]+$)");
        File downloads = new File("D://Programare in Retea//SocketHTTP//img//");

//        for (String link : allPhotosLinks) {
//
//            ImageDownloader.getAndWrite(link,downloads);
//        }

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
