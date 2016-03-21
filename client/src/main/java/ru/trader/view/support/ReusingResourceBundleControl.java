package ru.trader.view.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReusingResourceBundleControl extends ResourceBundle.Control {


    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        if (format.equals("java.properties")) {
            final String resourceName = toResourceName(bundleName, "properties");
            if (resourceName == null) {
                return bundle;
            }
            InputStream stream;
            try {
                stream = AccessController.doPrivileged(
                        (PrivilegedExceptionAction<InputStream>) () -> {
                            InputStream is = null;
                            if (reload) {
                                URL url = loader.getResource(resourceName);
                                if (url != null) {
                                    URLConnection connection = url.openConnection();
                                    if (connection != null) {
                                        // Disable caches to get fresh data for
                                        // reloading.
                                        connection.setUseCaches(false);
                                        is = connection.getInputStream();
                                    }
                                }
                            } else {
                                is = loader.getResourceAsStream(resourceName);
                            }
                            return is;
                        });
            } catch (PrivilegedActionException e) {
                throw (IOException) e.getException();
            }
            if (stream != null) {
                try {
                    bundle = new ReusingResourceBundle(stream);
                } finally {
                    stream.close();
                }
            }
        } else {
            throw new IllegalArgumentException("unknown format: " + format);
        }
        return bundle;
    }

    private class ReusingResourceBundle extends PropertyResourceBundle {
        public ReusingResourceBundle(InputStream stream) throws IOException {
            super(stream);
        }

        public ReusingResourceBundle(Reader reader) throws IOException {
            super(reader);
        }

        @Override
        public Object handleGetObject(String key) {
            try {
                return replaceKey(key);
            } catch (MissingResourceException ex) {
                return super.handleGetObject(key);
            }
        }

        private Pattern pattern = Pattern.compile("\\$\\{([\\w\\.\\-]+)}");

        private String replaceKey(String key) {
            String message = (String) super.handleGetObject(key);
            if (message != null) {
                StringBuffer sb = new StringBuffer();
                Matcher matcher = pattern.matcher(message);
                while (matcher.find()) {
                    matcher.appendReplacement(sb, replaceKey(matcher.group(1)));
                }
                matcher.appendTail(sb);
                return sb.toString();
            }
            throw new MissingResourceException("Can't find resource for bundle "
                    + this.getClass().getName()
                    +", key "+key,
                    this.getClass().getName(),
                    key);
        }
    }

}
