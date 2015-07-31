package ru.trader.core;

import ru.trader.analysis.AnalysisCallBack;

public class MarketAnalyzerCallBack {
    private final AnalysisCallBack parent;
    public final String ANALYSIS_STAGE = "market.stage.analysis";

    public MarketAnalyzerCallBack(AnalysisCallBack parent) {
        this.parent = parent;
    }

    public void start(int count){
        parent.startStage(ANALYSIS_STAGE);
        parent.print(String.format(parent.getMessage(ANALYSIS_STAGE)));
        parent.setMax(count);
    }

    public void inc(){
        parent.inc();
    }

    public void end(){
        parent.endStage(ANALYSIS_STAGE);
    }

    public boolean isCancel(){
        return parent.isCancel();
    }

    public AnalysisCallBack getParent() {
        return parent;
    }
}
