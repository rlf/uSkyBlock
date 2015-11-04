package us.talabrek.ultimateskyblock.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by R4zorax on 04/11/2015.
 */
public enum IslandUtil {;

    public static FilenameFilter createIslandFilenameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null
                        && name.matches("-?[0-9]+,-?[0-9]+.yml")
                        && !"null.yml".equalsIgnoreCase(name)
                        && !"0,0.yml".equalsIgnoreCase(name);
            }
        };
    }

}
