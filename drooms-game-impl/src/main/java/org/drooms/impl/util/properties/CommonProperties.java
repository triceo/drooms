package org.drooms.impl.util.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public abstract class CommonProperties {

    protected static Properties loadPropertiesFromFile(final File f) {
        try (InputStream is = new FileInputStream(f)) {
            return CommonProperties.loadPropertiesFromInputStream(is);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed reading properties from file: " + f);
        }
    }

    protected static Properties loadPropertiesFromInputStream(final InputStream is) throws IOException {
        final Properties props = new Properties();
        props.load(is);
        return props;
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
