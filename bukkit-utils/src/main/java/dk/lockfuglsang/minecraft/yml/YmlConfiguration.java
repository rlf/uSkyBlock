package dk.lockfuglsang.minecraft.yml;

import com.google.common.collect.Streams;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A YamlConfiguration that supports comments
 * <p>
 * Note: This includes a VERY SIMPLISTIC Yaml-parser, which sole purpose is to detect and store comments.
 */
@Deprecated(forRemoval = true) // Bukkit YamlConfiguration now preserves comments by default
public class YmlConfiguration extends YamlConfiguration {

    @Deprecated(forRemoval = true) // Use #getComments and #getInlineComments instead
    public String getComment(String key) {
        String comment = Streams.concat(super.getComments(key).stream(),
            super.getInlineComments(key).stream()).collect(Collectors.joining(System.lineSeparator()));
        if (comment.isEmpty()) {
            return null;
        }
        return comment.replaceAll("^# ?", "").replaceAll("\n# ?", "");
    }

    @Deprecated(forRemoval = true) // Use #getComments and #getInlineComments instead
    public Map<String, String> getComments() {
        Map<String, String> comments = new LinkedHashMap<>();
        for (String key : getKeys(true)) {
            String comment = getComment(key);
            if (comment != null) {
                comments.put(key, comment);
            }
        }
        return comments;
    }

    @Deprecated(forRemoval = true) // Use #setComments and #setInlineComments instead
    public void addComment(String path, String comment) {
        var comments = new ArrayList<>(super.getComments(path));
        comments.add(comment);
        super.setComments(path, comments);
    }

    @Deprecated(forRemoval = true) // Use #setComments and #setInlineComments instead
    public void addComments(Map<String, String> comments) {
        comments.forEach(this::addComment);
    }
}
