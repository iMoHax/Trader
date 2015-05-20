package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MarketUtils {
    private final static Logger LOG = LoggerFactory.getLogger(MarketUtils.class);

    public static List<Order> getStack(List<Order> orders, double balance, long cargo){
        LOG.trace("Fill stack orders {}, balance {}", orders, balance);
        orders.forEach(o -> o.setMax(balance, cargo));
        LOG.trace("Simple sort");
        orders.sort(Comparator.<Order>reverseOrder());
        LOG.trace("New order of orders {}", orders);
        List<Order> stack = new ArrayList<>();
        long count = cargo;
        double remain = balance;
        for (Order order : orders) {
            order = new Order(order.getSell(), order.getBuy(), remain, count);
            LOG.trace("Next best order {}", order);
            if (order.getProfit() > 0) {
                stack.add(order);
                remain -= order.getCount() * order.getSell().getPrice();
                count -= order.getCount();
                LOG.trace("Remain cargo {}, remain balance {}", count, remain);
            } else {
                LOG.trace("Low profit, stopped");
                remain = 0;
            }
            if (count <= 0 || remain <= 0) {
                break;
            }
        }
        LOG.trace("Stack: {}", stack);
        return stack;
    }

    public static List<Order> getOrders(Vendor seller, Vendor buyer){
        LOG.trace("Get orders from {}, to {}", seller, buyer);
        List<Order> orders = new ArrayList<>();
        for (Offer sell : seller.getAllSellOffers()) {
            Offer buy = buyer.getBuy(sell.getItem());
            if (buy != null) {
                Order order = new Order(sell, buy, 1);
                orders.add(order);
            }
        }
        return orders;
    }
}
