package com.company;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ImageDownloaderUtmMd extends Thread{

    private String urlString;

    public ImageDownloaderUtmMd(String urlString, File path) throws IOException, URISyntaxException, MalformedURLException, FileNotFoundException {

        this.urlString = urlString;

        // Generate a pathname to write the image to.
        String toWriteTo = path.toPath().toString() + System.getProperty("file.separator");

        // Convert String to URL.
        URL url = new URL(urlString);

        // Connect to the HTTP host and send the GET request for the image.
        SSLSocketFactory factory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket =
                (SSLSocket)factory.createSocket("www.verisign.com", 443);

        socket.startHandshake();
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("GET " + url.getPath() + " HTTP/1.1");
        pw.println("Host: " + url.getHost());
        pw.println();
        pw.flush();

        // Initialize the streams.
        final FileOutputStream fileOutputStream = new FileOutputStream(toWriteTo + url.getPath().replaceAll(".*/", ""));
        final InputStream inputStream = socket.getInputStream();

// Header end flag.
        boolean headerEnded = false;

        byte[] bytes = new byte[2048];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            // If the end of the header had already been reached, write the bytes to the file as normal.
            if (headerEnded)
                fileOutputStream.write(bytes, 0, length);

                // This locates the end of the header by comparing the current byte as well as the next 3 bytes
                // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
                // If the end of the header is reached, the flag is set to true and the remaining data in the
                // currently buffered byte array is written into the file.
            else {
                for (int i = 0; i < 2048; i++) {
                    if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                        headerEnded = true;
                        fileOutputStream.write(bytes, i+4, 2048-i-4);
                        break;
                    }
                }
            }
        }
        inputStream.close();
        fileOutputStream.close();
    }
}
