package com.cruxlab.parkurbn.model.response;

import com.cruxlab.parkurbn.model.History;

import java.util.List;

/**
 * Created by alla on 6/19/17.
 */

public class HistoryResponse {

    private List<History> transactions;

    public List<History> getTransactions() {
        return transactions;
    }
}
