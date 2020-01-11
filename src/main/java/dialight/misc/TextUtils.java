package dialight.misc;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    public static String readText(InputStream is) {
        return readText(is, Charset.defaultCharset());
    }
    public static String readText(InputStream is, Charset charset) {
        return readText(new InputStreamReader(is, charset));
    }
    public static String readText(Reader r) {
        try(Reader r2 = r) {
            char[] buffer = new char[4096];
            StringBuilder sb = new StringBuilder();
            int n;
            while ((n = r.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String readText(File file) {
        try {
            return readText(new FileInputStream(file), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeText(File file, String content) {
        try(OutputStream os = new FileOutputStream(file)) {
            writeText(os, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeText(OutputStream os, String content) {
        try {
            final Charset cs = Charset.defaultCharset();
            os.write(content.getBytes(cs));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readLines(InputStream is) {
        return readLines(new InputStreamReader(is, Charset.defaultCharset()));
    }
    public static List<String> readLines(Reader r) {
        try(BufferedReader reader = new BufferedReader(r)) {
            final List<String> list = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<String> readLines(File file) {
        try {
            return readLines(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeLines(OutputStream os, List<String> lines) {
        try {
            final Charset cs = Charset.defaultCharset();
            for (final Object line : lines) {
                if (line != null) {
                    os.write(line.toString().getBytes(cs));
                }
                os.write(System.lineSeparator().getBytes(cs));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeLines(Writer w, List<String> lines) {
        try {
            for (final String line : lines) {
                if (line != null) {
                    w.write(line);
                }
                w.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeLines(File file, List<String> lines) {
        try(OutputStream os = new FileOutputStream(file)) {
            writeLines(os, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
