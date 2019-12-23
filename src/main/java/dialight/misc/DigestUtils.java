package dialight.misc;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    private static int BUF_SIZE = 1024 * 4;

    private static char[] charRange(char fr, char to) {
        int count = to - fr + 1;
        if(count < 0) throw new RuntimeException("bad char order");
        char[] range = new char[count];
        for (int i = 0; i < count; i++) {
            range[i] = (char) (fr + i);
        }
        return range;
    }
    private static char[] join(char[]... arrs) {
        int total = 0;
        for (char[] arr : arrs) {
            total += arr.length;
        }
        char[] res = new char[total];
        int pos = 0;
        for (char[] arr : arrs) {
            System.arraycopy(arr, 0, res, pos, arr.length);
            pos += arr.length;
        }
        return res;
    }

    private static final char[] abc = join(charRange('0', '9'), charRange('a', 'z'));

    public static String toHex(byte[] ba) {
        StringBuilder sb = new StringBuilder();
        for (byte b : ba) {
            sb.append(abc[(b >> 4) & 0x0F]);
            sb.append(abc[b & 0x0F]);
        }
        return sb.toString();
    }

    public static byte[] digest(String algorithm, File file) {
        try(FileInputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return update(digest, is).digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static MessageDigest update(MessageDigest digest, InputStream is) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        int read;
        while ((read = is.read(buf, 0, BUF_SIZE)) != -1) {
            digest.update(buf, 0, read);
        }
        return digest;
    }

    public static String sha1(File jar) {
        return toHex(DigestUtils.digest("SHA-1", jar));
    }

}
