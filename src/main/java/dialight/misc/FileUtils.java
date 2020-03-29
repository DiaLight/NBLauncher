package dialight.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class FileUtils {

    public static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        Files.walkFileTree(directoryToBeDeleted, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void write(InputStream is, long total, Path jar, BiConsumer<Long, Long> progress) throws IOException {
        int BUF_SIZE = 1024;
        byte[] buf = new byte[BUF_SIZE];
        try (OutputStream os = Files.newOutputStream(jar)) {
            long totalRead = 0;
            int read;
            while ((read = is.read(buf, 0, BUF_SIZE)) != -1) {
                os.write(buf, 0, read);
                totalRead += read;
                progress.accept(totalRead, total);
            }
        }
    }
    public static boolean download(String url, Path jar, BiConsumer<Long, Long> progress) {
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
