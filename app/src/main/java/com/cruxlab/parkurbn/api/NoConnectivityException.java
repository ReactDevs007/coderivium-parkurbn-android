package com.cruxlab.parkurbn.api;

import java.io.IOException;

/**
 * Created by alla on 5/26/17.
 */

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return "The internet connection unavailable.";
    }

}
