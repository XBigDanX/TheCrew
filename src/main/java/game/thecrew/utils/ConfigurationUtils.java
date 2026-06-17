package game.thecrew.utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationUtils {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationUtils.class.getName());
    private static final String JNDI_FACTORY =
            "com.sun.jndi.fscontext.RefFSContextFactory";

    private static final String CONFIG_DIR = "file:C:/conf";
    private static final String CONFIGURATION_FILE_NAME = "application.properties";

    private ConfigurationUtils() {
    }

    private static Context init() throws NamingException {
        HashMap<String, String> env = new HashMap<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, CONFIG_DIR);

        return new InitialContext(new Hashtable<>(env));
    }

    public static String getKey(String key) {
        Context ctx = null;
        try {
            ctx = init();
            Object obj = ctx.lookup(CONFIGURATION_FILE_NAME);
            Properties props = new Properties();
            try (FileReader reader = new FileReader(obj.toString())) {
                props.load(reader);
            }
            return props.getProperty(key);
        } catch (NamingException | IOException e) {
            LOGGER.log(Level.SEVERE, "JNDI error for key ''{0}'': {1}", new Object[]{key, e.getMessage()});
            return null;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e2) {
                    /* ignore */
                }
            }
        }
    }
}