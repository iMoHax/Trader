package ru.trader.graph;

import ru.trader.core.Vendor;

import java.util.LinkedList;
import java.util.List;

public class RouteSearcherCallBack {

    private volatile boolean cancel = false;
    private final List<GraphCallBack<Vendor>> callbacks = new LinkedList<>();

    public final GraphCallBack<Vendor> onStart(){
        GraphCallBack<Vendor> callback = getGraphCallBackInstance();
        if (cancel) return callback;
        synchronized (callbacks) {
            callbacks.add(callback);
        }
        return callback;
    }

    public final void onEnd( GraphCallBack<Vendor> callback){
        synchronized (callbacks) {
            callbacks.remove(callback);
        }
    }

    protected GraphCallBack<Vendor> getGraphCallBackInstance(){
        return new GraphCallBack<>();
    }

    public final boolean isCancel() {
        return cancel;
    }

    public final void cancel(){
        if (cancel) return;
        this.cancel = true;
        synchronized (callbacks){
            callbacks.forEach(GraphCallBack::cancel);
            callbacks.clear();
        }
    }
}
