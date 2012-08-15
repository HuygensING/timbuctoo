package nl.knaw.huygens.repository.util;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class Configuration {
    private static final String DEFAULT_CONFIG_FILE = "../config.xml";
    private static final String SETTINGS_PREFIX = "settings.";
    private XMLConfiguration xmlConfig;

    public Configuration() throws ConfigurationException {
        this(DEFAULT_CONFIG_FILE);
    }

    public Configuration(String configFile) throws ConfigurationException {
        xmlConfig = new XMLConfiguration();
        xmlConfig.clear();
        InputStream in = Configuration.class.getResourceAsStream(configFile);
        try {
            xmlConfig.load(in);
        } catch (ConfigurationException e) {
            System.err.println("ERROR: unable to load configuration!");
            throw e;
        }
    }
    
    public String getSetting(String setting, String defaultValue) {
        return xmlConfig.getString(SETTINGS_PREFIX + setting, defaultValue);
    }
}
