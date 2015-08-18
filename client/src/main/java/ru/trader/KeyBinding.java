package ru.trader;

import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;

public class KeyBinding {
    private final static Provider provider = Provider.getCurrentProvider(false);

    public static void bind(KeyStroke keys, HotKeyListener listener){
        provider.register(keys, listener);
    }

    public static void unbind(){
        provider.reset();
        provider.stop();
    }


}
