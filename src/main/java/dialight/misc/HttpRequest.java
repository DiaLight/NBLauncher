package dialight.misc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    public static class Resource {

        public final String text;
        public final Date lastModified;
        public final String eTag;
        public final int contentLength;

        public Resource(String text, Date lastModified, String eTag, int contentLength) {
            this.text = text;
            this.lastModified = lastModified;
            this.eTag = eTag;
            this.contentLength = contentLength;
        }

    }

    public static Resource get(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);


        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "NBLauncher");
        con.connect();

        long dateField = con.getHeaderFieldDate("Last-Modified", 0);
        Date lastModified = dateField != 0 ? new Date(dateField) : null;
        String eTag = con.getHeaderField("ETag");
        int contentLength = con.getHeaderFieldInt("Content-Length", 0);

        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
                sb.append("\n");
            }
        }
        return new Resource(sb.toString(), lastModified, eTag, contentLength);
    }

    public static Resource head(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);


        con.setRequestMethod("HEAD");
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "NBLauncher");
        con.connect();

        long dateField = con.getHeaderFieldDate("Last-Modified", 0);
        Date lastModified = dateField != 0 ? new Date(dateField) : null;
        String eTag = con.getHeaderField("ETag");
        int contentLength = con.getHeaderFieldInt("Content-Length", 0);

        return new Resource(null, lastModified, eTag, contentLength);
    }

    public static String post(String url, String content, String contentType) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "NBLauncher");
        con.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        con.setRequestProperty("Content-Length", "" + bytes.length);
        try (OutputStream os = con.getOutputStream()) {
            os.write(bytes);
        }

        try {
            return TextUtils.readText(con.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            InputStream stderr = con.getErrorStream();
            if (stderr == null) throw e;
            return TextUtils.readText(stderr, StandardCharsets.UTF_8);
        }
    }

}
