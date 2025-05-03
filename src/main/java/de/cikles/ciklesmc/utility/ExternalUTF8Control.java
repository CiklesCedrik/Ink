package de.cikles.ciklesmc.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExternalUTF8Control extends ResourceBundle.Control {

    private final File directory;

    public ExternalUTF8Control(File directory) {
        this.directory = directory;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return Collections.singletonList("properties");
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                    ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {

        if (!format.equals("properties")) {
            return null;
        }

        String bundleName = toBundleName(baseName, locale);
        String resourceName = bundleName + ".properties";

        File file = new File(directory, resourceName);
        if (!file.exists()) return null;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
}
