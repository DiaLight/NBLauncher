package dialight.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class FileUtils {

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static void write(InputStream is, long total, File jar, BiConsumer<Long, Long> progress) throws IOException {
        int BUF_SIZE = 1024;
        byte[] buf = new byte[BUF_SIZE];
        try (FileOutputStream os = new FileOutputStream(jar)) {
            long totalRead = 0;
            int read;
            while ((read = is.read(buf, 0, BUF_SIZE)) != -1) {
                os.write(buf, 0, read);
                totalRead += read;
                progress.accept(totalRead, total);
            }
        }
    }
    public static boolean download(String url, File jar, BiConsumer<Long, Long> progress) {
        try {
            URLConnection con = new URL(url).openConnection();
            con.setUseCaches(false);
            con.setRequestProperty("User-Agent", "NBLauncher");
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            try {
                con.connect();
            } catch (SocketTimeoutException ignore) {
                return false;
            } finally {

            }
            int total = con.getContentLength();
            try(InputStream is = con.getInputStream()) {
                write(is, total, jar, progress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NotNull public static Manifest getManifest() {
        URLClassLoader cl = (URLClassLoader) FileUtils.class.getClassLoader();
        try {
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest();
            try(InputStream is = url.openStream()) {
                manifest.read(is);
            }
            return manifest;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
