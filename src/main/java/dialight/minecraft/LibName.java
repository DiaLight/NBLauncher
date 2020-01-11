package dialight.minecraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibName {

    private static final Pattern NAME_PATTERN = Pattern.compile("([\\w-._]+):([\\w-._]+):([\\w\\d-+._]+)(:([\\w\\d-._]+))?");

    @NotNull private final String groupId;
    @NotNull private final String artifactId;
    @NotNull private final String version;
    @Nullable private final String scope;

    public LibName(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
    }

    @NotNull public String getGroupId() {
        return groupId;
    }

    @NotNull public String getArtifactId() {
        return artifactId;
    }

    @NotNull public String getVersion() {
        return version;
    }

    @Nullable public String getScope() {
        return scope;
    }

    public static LibName parse(String libname) {
        Matcher matcher = NAME_PATTERN.matcher(libname);
        if(!matcher.matches()) throw new IllegalStateException("bad name " + libname);
        String groupId = matcher.group(1);
        String artifactId = matcher.group(2);
        String version = matcher.group(3);
        String scope = matcher.group(5);
        return new LibName(groupId, artifactId, version, scope);
    }

    public String buildPath() {
        String suffix = "";
        if(scope != null) {
            suffix = "-" + scope;
        }
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + suffix + ".jar";
    }
}
