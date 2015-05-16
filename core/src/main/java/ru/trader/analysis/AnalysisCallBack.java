package ru.trader.analysis;

public class AnalysisCallBack {

    private volatile boolean cancel = false;

    public String getMessage(String key){return "";}

    public void startStage(String id){}
    public void setMax(long count){}
    public void inc(){}
    public void print(String message){}
    public void endStage(String id){}

    public final boolean isCancel() {
        return cancel;
    }

    public final void cancel(){
        this.cancel = true;
    }

}
