package game.thecrew.utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

public class ConfigurationUtils {

    private static final String JNDI_FACTORY =
            "com.sun.jndi.fscontext.RefFSContextFactory";

    private static final String CONFIG_DIR = "file:C:/conf";
    private static final String configurationFileName = "application.properties";

    private static Context init() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, CONFIG_DIR);

        return new InitialContext(env);
    }

    public static String getKey(String key) {
        Context ctx = null;
        try {
            ctx = init();
            Object obj = ctx.lookup(configurationFileName);
            Properties props = new Properties();
            // obj.toString() returns the full path to the file found by JNDI
            try(FileReader reader = new FileReader(obj.toString())) {
                props.load(reader);
            }
            return props.getProperty(key);
        } catch (NamingException | IOException e) {
            System.err.println("JNDI error for key '" + key + "': " + e.getMessage());
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