package ru.trader.core;

import ru.trader.graph.GraphCallBack;
import ru.trader.graph.RouteSearcherCallBack;

public class MarketAnalyzerCallBack {
    private volatile boolean cancel = false;
    private RouteSearcherCallBack callbackRoute;
    private GraphCallBack<Place> callbackGraph;


    protected RouteSearcherCallBack getRouteSearcherCallBackInstance(){
        return new RouteSearcherCallBack();
    }

    protected GraphCallBack<Place> getGraphCallBackInstance(){
        return new GraphCallBack<>();
    }

    public final GraphCallBack<Place> onStartGraph(){
        callbackGraph = getGraphCallBackInstance();
        return callbackGraph;
    }

    public final void onEndGraph(){
        callbackGraph = null;
    }

    public final RouteSearcherCallBack onStartSearch(){
        callbackRoute = getRouteSearcherCallBackInstance();
        return callbackRoute;
    }

    public final void onEndSearch(){
        callbackRoute = null;
        onEnd();
    }

    protected void onEnd(){}

    public void setMax(long max){}
    public void inc(){}


    public final boolean isCancel() {
        return cancel;
    }

    public final void cancel(){
        if (cancel) return;
        this.cancel = true;
        if (callbackRoute != null){
            callbackRoute.cancel();
            callbackRoute = null;
        }
        if (callbackGraph != null){
            callbackGraph.cancel();
            callbackGraph = null;
        }
    }

}
