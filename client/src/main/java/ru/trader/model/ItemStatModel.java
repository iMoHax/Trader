package ru.trader.model;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.ItemStat;
import ru.trader.core.Offer;

import java.util.List;
import java.util.stream.Collectors;

public class ItemStatModel {
    private final static Logger LOG = LoggerFactory.getLogger(ItemStatModel.class);
    private ItemStat itemStat;
    private final MarketModel market;

    private DoubleProperty avg;
    private ObjectProperty<OfferModel> max;
    private ObjectProperty<OfferModel> min;
    private ObjectProperty<OfferModel> best;

    ItemStatModel(ItemStat itemStat, MarketModel market) {
        this.itemStat = itemStat;
        this.market = market;
    }

    public ReadOnlyDoubleProperty avgProperty(){
        if (avg == null) avg = new SimpleDoubleProperty(itemStat.getAvg());
        return avg;
    }

    public ReadOnlyObjectProperty<OfferModel> minProperty(){
        if (min == null){
            min = new SimpleObjectProperty<>(market.asModel(itemStat.getMin()));
        }
        return min;
    }

    public ReadOnlyObjectProperty<OfferModel> maxProperty(){
        if (max == null) {
            max = new SimpleObjectProperty<>(market.asModel(itemStat.getMax()));
        }
        return max;
    }

    public ReadOnlyObjectProperty<OfferModel> bestProperty(){
        if (best == null){
            best = new SimpleObjectProperty<>(market.asModel(itemStat.getBest()));
        }
        return best;
    }

    public double getAvg() {
        return avg != null ? avg.get() : itemStat.getAvg();
    }

    public OfferModel getMax() {
        return max != null ? max.get() : market.asModel(itemStat.getMax());
    }

    public OfferModel getMin() {
        return min != null ? min.get() : market.asModel(itemStat.getMin());
    }

    public OfferModel getBest() {
        return best != null ? best.get() : market.asModel(itemStat.getBest());
    }

    public List<OfferModel> getOffers() {
        return itemStat.getOffers().stream().map(market::asModel).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        if (LOG.isTraceEnabled()){
            sb.append("ItemStatModel{");
            sb.append("avgProp=").append(avg);
            sb.append(", maxProp=").append(max);
            sb.append(", minProp=").append(min);
            sb.append(", bestProp=").append(best);
            sb.append(", itemStat=").append(itemStat.toString());
            sb.append('}');
            return sb.toString();
        }
        sb.append("avg=").append(getAvg());
        sb.append(", max=").append(getMax());
        sb.append(", min=").append(getMin());
        sb.append(", best=").append(getBest());

        return sb.toString();
    }

    public void refresh(){
        LOG.debug("Refresh model {}", this);
        if (itemStat.isEmpty()) {
            ItemStat fresh = market.getStat(itemStat.getType(), itemStat.getItem());
            if (itemStat != fresh) itemStat = fresh;
        }
        if (avg!=null) avg.setValue(itemStat.getAvg());
        refreshProp(min, itemStat.getMin());
        refreshProp(max, itemStat.getMax());
        refreshProp(best, itemStat.getBest());
        LOG.debug("Fresh model = {}", this);
    }

    private void refreshProp(ObjectProperty<OfferModel> prop, Offer offer){
        if (prop!=null ){
            OfferModel model = prop.getValue();
            if (model==null || !model.isModel(offer)){
                prop.setValue(market.asModel(offer));
            }
        }
    }

}
