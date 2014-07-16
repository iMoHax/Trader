package ru.trader.core;


public enum OFFER_TYPE {
    SELL,
    BUY {
        @Override
        public int getOrder() {
            return -1;
        }
    };

    public int getOrder(){
        return 1;
    }

}
