package ru.trader;

import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class KeyBinding {
    private final static Provider provider = Provider.getCurrentProvider(false);
    private final static Map<KeyStroke, HotKeyListener> hotKeys = new HashMap<>(10, 0.9f);

    public static void bind(KeyStroke keys, HotKeyListener listener){
        if (keys == null) return;
        provider.register(keys, listener);
        hotKeys.put(keys, listener);
    }

    public static void unbind(KeyStroke hotKey){
        if (hotKey == null) return;
        provider.reset();
        hotKeys.remove(hotKey);
        hotKeys.forEach(provider::register);
    }

    public static void unbind(){
        provider.reset();
        provider.stop();
        hotKeys.clear();
    }


}
