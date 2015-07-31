package ru.trader.analysis.graph;

import ru.trader.analysis.AnalysisCallBack;

public class CrawlerCallBack {
    private final AnalysisCallBack parent;
    public final String SEARCH_STAGE = "crawler.stage.search";

    public CrawlerCallBack(AnalysisCallBack parent) {
        this.parent = parent;
    }

    public void startSearch(Object from, Object to, int count){
        parent.startStage(SEARCH_STAGE);
        parent.print(String.format(parent.getMessage(SEARCH_STAGE), from.toString(), to.toString()));
        parent.setMax(count);
    }

    public void found(){
        parent.inc();
    }

    public void endSearch(){
        parent.endStage(SEARCH_STAGE);
    }

    public boolean isCancel(){
        return parent.isCancel();
    }
}
