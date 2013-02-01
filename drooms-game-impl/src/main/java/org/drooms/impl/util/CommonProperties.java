package org.drooms.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class CommonProperties {

    protected static Properties loadPropertiesFromFile(final File f) {
        try (InputStream is = new FileInputStream(f)) {
            final Properties props = new Properties();
            props.load(is);
            return props;
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed reading properties from file: " + f);
        }
    }

    private final Properties properties;

    protected CommonProperties(final Properties p) {
        this.properties = p;
    }

    protected String getMandatoryProperty(final String key) {
        final String value = this.properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Mandatory property not found: " + key);
        }
        return value;
    }

    protected String getOptionalProperty(final String key, final String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    public Collection<Map.Entry<Object, Object>> getTextEntries() {
        return Collections.unmodifiableCollection(this.properties.entrySet());
    }

}
