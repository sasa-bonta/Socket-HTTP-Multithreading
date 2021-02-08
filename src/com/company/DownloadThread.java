package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class DownloadThread extends Thread{

    final File downloads = new File("D://Programare in Retea//SocketHTTP//img//");

    private URL imageURL;
    FileOutputStream fos;

    public DownloadThread (String imageURL, String fileName) throws URISyntaxException, MalformedURLException, FileNotFoundException {
        URI uri = new URI(imageURL);
        this.imageURL = uri.toURL();
        File outputFile = new File(downloads, String.valueOf(fileName));
        this.fos = new FileOutputStream(outputFile);
    }

    public File getDownloads() {
        return downloads;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public void setImageURL(URL imageURL) {
        this.imageURL = imageURL;
    }

    public FileOutputStream getFos() {
        return fos;
    }

    public void setFos(FileOutputStream fos) {
        this.fos = fos;
    }

    @Override
    public void run() {
        //my code here
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageURL);
            ImageIO.write(image, "png", fos);
            ImageIO.write(image, "jpg", fos);
            ImageIO.write(image, "gif", fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
