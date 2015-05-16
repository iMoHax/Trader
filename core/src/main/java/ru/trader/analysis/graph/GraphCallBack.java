package ru.trader.analysis.graph;

import ru.trader.analysis.AnalysisCallBack;

public class GraphCallBack {
    private final AnalysisCallBack parent;
    public final String BUILD_STAGE = "graph.stage.build";

    public GraphCallBack(AnalysisCallBack parent) {
        this.parent = parent;
    }

    public void startBuild(Object entry){
        parent.startStage(BUILD_STAGE);
        parent.print(String.format(parent.getMessage(BUILD_STAGE), entry.toString()));
    }

    public void endBuild(){
        parent.endStage(BUILD_STAGE);
    }

    public boolean isCancel(){
        return parent.isCancel();
    }
}
