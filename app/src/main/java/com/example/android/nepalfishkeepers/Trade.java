package com.example.android.nepalfishkeepers;

import java.util.HashMap;
import java.util.Map;

public class Trade {
    private String traderId, tradeOwner, collectionId;
    private TradeStatus tradeStatus;
    public Map<String, String> sharers = new HashMap<>();

    public Trade() {

    }

    public Trade(String traderId, String tradeStatus, String tradeOwner, String collectionId) {
        this.tradeOwner = tradeOwner;
        this.traderId = traderId;
        this.collectionId = collectionId;
        this.tradeStatus = TradeStatus.valueOf(tradeStatus);
    }

    public String getTraderId() {
        return traderId;
    }

    public void setTraderId(String traderId) {
        this.traderId = traderId;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getTradeOwner() {
        return tradeOwner;
    }

    public void setTradeOwner(String tradeOwner) {
        this.tradeOwner = tradeOwner;
    }
}
