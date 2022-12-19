package com.rrtry.id3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class URLPicture {

    private URL url;

    public URLPicture(URL url) {
        this.url = url;
    }

    public URLPicture(String urlString) throws MalformedURLException {
        this.url = new URL(urlString);
    }

    public URLPicture(byte[] stringBytes) throws MalformedURLException {
        this.url = new URL(
                new String(stringBytes, StandardCharsets.ISO_8859_1).replace("\0", "")
        );
    }

    public URL getUrl() {
        return url;
    }

    public String getMimeType() throws IOException {
        return url.openConnection().getContentType();
    }

    public String getExtension() throws IOException {
        String mimeType = getMimeType();
        return "." + mimeType.substring(mimeType.lastIndexOf("/") + 1);
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public byte[] getPictureBytes() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            int data;
            while ((data = inputStream.read()) != -1) {
                outputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
