package ru.trader.view.support;

import ru.trader.Main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Localization {
    private static final Locale DEFAULT = new Locale("ru", "RU");
    private static final ClassLoader loader = getLoader();
    private static final Locale[] supportedLocales = {
        DEFAULT,
        new Locale("ru", "RU", "FULL"),
        new Locale("en", "US")
    };

    private static ResourceBundle rb = ResourceBundle.getBundle("locale", DEFAULT, loader, new ReusingResourceBundleControl());
    private static Locale locale = DEFAULT;


    static {
        setLocale(getSupported(Locale.getDefault()));
    }

    private static Locale getSupported(Locale locale){
        for (Locale l : supportedLocales) {
            if (l.getLanguage().equals(locale.getLanguage()))
                return l;
        }
        return DEFAULT;
    }

    private static ClassLoader getLoader() {
        try {
             URL url = Main.class.getResource("/lang/");
             File file = new File("lang");
             if (file.exists() && file.isDirectory())
                url = file.toURI().toURL();
             return new URLClassLoader(new URL[]{url});
        } catch (MalformedURLException e) {
            return ClassLoader.getSystemClassLoader();
        }
    }

    public static void setLocale(Locale locale){
        Locale.setDefault(locale);
        Localization.locale = locale;
        Main.SETTINGS.setLocale(locale);
        rb = getResources(locale);
    }

    public static ResourceBundle getResources(Locale locale){
        return ResourceBundle.getBundle("locale", locale, loader, new ReusingResourceBundleControl());
    }

    public static ResourceBundle getResources(){
        return rb;
    }

    public static Collection<Locale> getLocales(){
        return Arrays.asList(supportedLocales);
    }

    public static String getString(String key){
        return rb.getString(key);
    }

    public static String getString(String key, String def){
        try {
            return rb.getString(key);
        } catch (MissingResourceException e){
            return def;
        }
    }

    public static Locale getCurrentLocale() {
        return locale;
    }
}

