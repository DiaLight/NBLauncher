package dialight.misc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpRequest {

    public static String read(String url) throws IOException {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String post(String url, String content, String contentType) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        con.setRequestMethod("POST");
        con.setDoOutput(true);
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
