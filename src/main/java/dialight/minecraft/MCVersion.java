package dialight.minecraft;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MCVersion  implements Comparable<MCVersion> {

    private int major;
    private int minor;
    private int patch;

    public MCVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }
    public MCVersion() {
        this(0, 0, 0);
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(major);
        dos.writeInt(minor);
        dos.writeInt(patch);
    }

    public void read(DataInputStream dis) throws IOException {
        major = dis.readInt();
        minor = dis.readInt();
        patch = dis.readInt();
    }

    public static MCVersion parse(String version) {
        String[] split = version.split("\\.");
        if(split.length < 2 || split.length > 3) throw new NumberFormatException("Bad MCVersion format \"" + version + "\"");
        return new MCVersion(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split.length == 3 ? Integer.parseInt(split[2]) : 0);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public int compatibleHash() {
        int result = major;
        result = 31 * result + minor;
        return result;
    }

    @Override
    public int compareTo(@NotNull MCVersion that) {
        int compare = 0;
        compare = Integer.compare(this.major, that.major);
        if(compare != 0) return compare;
        compare = Integer.compare(this.minor, that.minor);
        if(compare != 0) return compare;
        compare = Integer.compare(this.patch, that.patch);
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MCVersion MCVersion = (MCVersion) o;

        if (major != MCVersion.major) return false;
        if (minor != MCVersion.minor) return false;
        return patch == MCVersion.patch;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }

    @Override
    public String toString() {
        if(patch == 0) return major + "." + minor;
        return major + "." + minor + "." + patch;
    }

    public boolean compatibleWith(MCVersion that) {
        if(this.major != that.major) return false;
        return this.minor == that.minor;
    }
}
