package ru.trader.analysis;

import ru.trader.analysis.graph.CrawlerSpecification;
import ru.trader.core.Vendor;

public interface VendorsCrawlerSpecification extends CrawlerSpecification<Vendor> {

    public double computeWeight(VendorsCrawler.VendorsEdge edge);

    public double computeWeight(VendorsCrawler.VendorsTraversalEntry entry);

}
