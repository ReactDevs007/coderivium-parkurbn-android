package com.cruxlab.parkurbn.api;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tech.gusavila92.websocketclient.WebSocketClient;

public class InfoUpdateManager {

    private static final String TAG = InfoUpdateManager.class.getSimpleName();

    private static final String SPOT_STATUS_URI = "ws://" + ParkUrbnApi.HOST_NAME + "/ws/ws_spots";
    private static final String SEGMENT_STATUS_URI = "ws://" + ParkUrbnApi.HOST_NAME + "/ws/ws_segments";
    private static final String SPOT_TIMES_URI = "ws://" + ParkUrbnApi.HOST_NAME + "/ws/ws_spot_info";
    private static final String SEGMENT_TIMES_URI = "ws://" + ParkUrbnApi.HOST_NAME + "/ws/ws_segment_info";

    private static final String SPOT_STATUS_WS_CLIENT_NAME = "Spot Status WS Client";
    private static final String SEGMENT_STATUS_WS_CLIENT_NAME = "Segment Status WS Client";
    private static final String SPOT_TIMES_WS_CLIENT_NAME = "Spot Times WS Client";
    private static final String SEGMENT_TIMES_WS_CLIENT_NAME = "Segment Times WS Client";

    public interface IInfoUpdate {

        void onSpotStateChanged(String id, boolean isTaken);

        void onSegmentFreeCountChanged(String id, int count);

        void onSpotTimesChanged(Map<String, Integer> times);

        void onSegmentTimesChanged(Map<String, Integer> times);

    }

    private URI mSpotStatusSocketURI;
    private URI mSegmentStatusSocketURI;
    private URI mSpotTimesSocketURI;
    private URI mSegmentTimesSocketURI;

    private BaseWebSocketClient mSpotStatusWSClient;
    private BaseWebSocketClient mSegmentStatusWSClient;
    private BaseWebSocketClient mSpotTimesWSClient;
    private BaseWebSocketClient mSegmentTimesWSClient;

    private Gson mGson;
    private IInfoUpdate mCallback;
    private Set<String> mLastSegmentsIds;
    private Set<String> mLastSpotsIds;

    private boolean receiveUpdates;

    public InfoUpdateManager(IInfoUpdate callback) {
        mCallback = callback;
        mGson = new Gson();
        initSocketURIs();
    }

    public void connectToSockets(Set<String> spotsIds, Set<String> segmentsIds) {
        receiveUpdates = true;
        subscribeSpots(spotsIds);
        subscribeSegments(segmentsIds);
    }

    public void subscribeSpots(Set<String> spotIds) {
        mLastSpotsIds = spotIds;
        if (mLastSpotsIds.size() == 0) {
            disconnect(SPOT_STATUS_WS_CLIENT_NAME);
            disconnect(SPOT_TIMES_WS_CLIENT_NAME);
        } else if (receiveUpdates) {
            subscribeOrConnect(SPOT_STATUS_WS_CLIENT_NAME);
            subscribeOrConnect(SPOT_TIMES_WS_CLIENT_NAME);
        }
    }

    public void subscribeSegments(Set<String> segmentIds) {
        mLastSegmentsIds = segmentIds;
        if (mLastSegmentsIds.size() == 0) {
            disconnect(SEGMENT_STATUS_WS_CLIENT_NAME);
            disconnect(SEGMENT_TIMES_WS_CLIENT_NAME);
        } else if (receiveUpdates) {
            subscribeOrConnect(SEGMENT_STATUS_WS_CLIENT_NAME);
            subscribeOrConnect(SEGMENT_TIMES_WS_CLIENT_NAME);
        }
    }

    public void disconnectFromSockets() {
        receiveUpdates = false;
        disconnect(SPOT_STATUS_WS_CLIENT_NAME);
        disconnect(SEGMENT_STATUS_WS_CLIENT_NAME);
        disconnect(SPOT_TIMES_WS_CLIENT_NAME);
        disconnect(SEGMENT_TIMES_WS_CLIENT_NAME);
    }

    private void connect(String clientName) {
        switch (clientName) {
            case SPOT_STATUS_WS_CLIENT_NAME:
                if (mLastSpotsIds.size() > 0) {
                    mSpotStatusWSClient = getSpotStatusWSClient();
                    mSpotStatusWSClient.connect();
                }
                break;
            case SEGMENT_STATUS_WS_CLIENT_NAME:
                if (mLastSegmentsIds.size() > 0) {
                    mSegmentStatusWSClient = getSegmentStatusWSClient();
                    mSegmentStatusWSClient.connect();
                }
                break;
            case SPOT_TIMES_WS_CLIENT_NAME:
                if (mLastSpotsIds.size() > 0) {
                    mSpotTimesWSClient = getSpotTimesWSClient();
                    mSpotTimesWSClient.connect();
                }
                break;
            case SEGMENT_TIMES_WS_CLIENT_NAME:
                if (mLastSegmentsIds.size() > 0) {
                    mSegmentTimesWSClient = getSegmentTimesWSClient();
                    mSegmentTimesWSClient.connect();
                }
                break;
        }
    }

    private void disconnect(String clientName) {
        switch (clientName) {
            case SPOT_STATUS_WS_CLIENT_NAME:
                if (mSpotStatusWSClient != null && !mSpotStatusWSClient.isClosed()) {
                    mSpotStatusWSClient.close();
                    mSpotStatusWSClient = null;
                }
                break;
            case SEGMENT_STATUS_WS_CLIENT_NAME:
                if (mSegmentStatusWSClient != null && !mSegmentStatusWSClient.isClosed()) {
                    mSegmentStatusWSClient.close();
                    mSegmentStatusWSClient = null;
                }
                break;
            case SPOT_TIMES_WS_CLIENT_NAME:
                if (mSpotTimesWSClient != null && !mSpotTimesWSClient.isClosed()) {
                    mSpotTimesWSClient.close();
                   mSpotTimesWSClient = null;
                }
                break;
            case SEGMENT_TIMES_WS_CLIENT_NAME:
                if (mSegmentTimesWSClient != null && !mSegmentTimesWSClient.isClosed()) {
                    mSegmentTimesWSClient.close();
                    mSegmentTimesWSClient = null;
                }
                break;
        }
    }

    private void subscribeOrConnect(String clientName) {
        switch (clientName) {
            case SPOT_STATUS_WS_CLIENT_NAME:
                if (mSpotStatusWSClient != null && !mSpotStatusWSClient.isClosed()) {
                    Log.i(TAG, "Subscribe spots to Spot Status WS Client: " + mLastSpotsIds);
                    mSpotStatusWSClient.send(mGson.toJson(mLastSpotsIds));
                } else {
                    connect(SPOT_STATUS_WS_CLIENT_NAME);
                }
                break;
            case SEGMENT_STATUS_WS_CLIENT_NAME:
                if (mSegmentStatusWSClient != null && !mSegmentStatusWSClient.isClosed()) {
                    Log.i(TAG, "Subscribe segments to Segment Status WS Client: " + mLastSegmentsIds);
                    mSegmentStatusWSClient.send(mGson.toJson(mLastSegmentsIds));
                } else {
                    connect(SEGMENT_STATUS_WS_CLIENT_NAME);
                }
                break;
            case SPOT_TIMES_WS_CLIENT_NAME:
                if (mSpotTimesWSClient != null && !mSpotTimesWSClient.isClosed()) {
                    Log.i(TAG, "Subscribe spots to Spot Times WS Client: " + mLastSpotsIds);
                    mSpotTimesWSClient.send(mGson.toJson(mLastSpotsIds));
                } else {
                    connect(SPOT_TIMES_WS_CLIENT_NAME);
                }
                break;
            case SEGMENT_TIMES_WS_CLIENT_NAME:
                if (mSegmentTimesWSClient != null && !mSegmentTimesWSClient.isClosed()) {
                    Log.i(TAG, "Subscribe segments to Segment Times WS Client: " + mLastSegmentsIds);
                    mSegmentTimesWSClient.send(mGson.toJson(mLastSegmentsIds));
                } else {
                    connect(SEGMENT_TIMES_WS_CLIENT_NAME);
                }
                break;
        }
    }


    private void initSocketURIs() {
        mSpotStatusSocketURI = getUri(SPOT_STATUS_URI);
        mSegmentStatusSocketURI = getUri(SEGMENT_STATUS_URI);
        mSpotTimesSocketURI = getUri(SPOT_TIMES_URI);
        mSegmentTimesSocketURI = getUri(SEGMENT_TIMES_URI);
    }

    private BaseWebSocketClient getSpotStatusWSClient() {
        return new BaseWebSocketClient(mSpotStatusSocketURI, SPOT_STATUS_WS_CLIENT_NAME) {

            @Override
            public void onOpen() {
                super.onOpen();
                if (mLastSpotsIds != null && mLastSpotsIds.size() > 0) {
                    subscribeSpots(mLastSpotsIds);
                }
            }

            @Override
            public void onTextReceived(String message) {
                super.onTextReceived(message);
                try {
                    JSONObject data = new JSONObject(message);
                    String id = data.names().get(0).toString();
                    mCallback.onSpotStateChanged(id, !data.opt(id).equals("FREE"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private BaseWebSocketClient getSegmentStatusWSClient() {
        return new BaseWebSocketClient(mSegmentStatusSocketURI, SEGMENT_STATUS_WS_CLIENT_NAME) {

            @Override
            public void onOpen() {
                super.onOpen();
                if (mLastSegmentsIds != null && mLastSegmentsIds.size() > 0) {
                    subscribeSegments(mLastSegmentsIds);
                }
            }

            @Override
            public void onTextReceived(String message) {
                super.onTextReceived(message);
                try {
                    JSONObject data = new JSONObject(message);
                    String id = data.names().get(0).toString();
                    mCallback.onSegmentFreeCountChanged(id, data.optInt(id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private BaseWebSocketClient getSpotTimesWSClient() {
        return new BaseWebSocketClient(mSpotTimesSocketURI, SPOT_TIMES_WS_CLIENT_NAME) {

            @Override
            public void onOpen() {
                super.onOpen();
                if (mLastSpotsIds != null && mLastSpotsIds.size() > 0) {
                    subscribeSpots(mLastSpotsIds);
                }
            }

            @Override
            public void onTextReceived(String message) {
                super.onTextReceived(message);
                try {
                    JSONArray data = new JSONArray(message);
                    Map<String, Integer> times = new HashMap<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject info = data.getJSONObject(i);
                        times.put(info.getString("id"), info.getInt("parking_time"));
                    }
                    mCallback.onSpotTimesChanged(times);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private BaseWebSocketClient getSegmentTimesWSClient() {
        return new BaseWebSocketClient(mSegmentTimesSocketURI, SEGMENT_TIMES_WS_CLIENT_NAME) {

            @Override
            public void onOpen() {
                super.onOpen();
                if (mLastSegmentsIds != null && mLastSegmentsIds.size() > 0) {
                    subscribeSegments(mLastSegmentsIds);
                }
            }

            @Override
            public void onTextReceived(String message) {
                super.onTextReceived(message);
                try {
                    JSONArray data = new JSONArray(message);
                    Map<String, Integer> times = new HashMap<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject info = data.getJSONObject(i);
                        times.put(info.getString("id"), info.getInt("parking_time"));
                    }
                    mCallback.onSegmentTimesChanged(times);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private URI getUri(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class BaseWebSocketClient extends WebSocketClient {

        private boolean isClosed;

        private String mClientName;

        private BaseWebSocketClient(URI uri, String clientName) {
            super(uri);
            mClientName = clientName;
        }

        @Override
        public void onOpen() {
            Log.i(TAG, mClientName + " opened");
        }

        @Override
        public void onTextReceived(String message) {
            Log.i(TAG, mClientName + " received text: " + message);
        }

        @Override
        public void onBinaryReceived(byte[] data) {
            Log.i(TAG, mClientName + " received binary");
        }

        @Override
        public void onPingReceived(byte[] data) {
            Log.i(TAG, mClientName + " received ping");
        }

        @Override
        public void onPongReceived(byte[] data) {
            Log.i(TAG, mClientName + " received pong");
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
            Log.i(TAG, mClientName + " error: " + e.getMessage());
            isClosed = true;
            InfoUpdateManager.this.connect(mClientName);
        }

        @Override
        public void onCloseReceived() {
            Log.i(TAG, mClientName + " received close");
            isClosed = true;
            InfoUpdateManager.this.connect(mClientName);
        }

        @Override
        public void close() {
            super.close();
            Log.i(TAG, mClientName + " closed");
            isClosed = true;
        }

        public boolean isClosed() {
            return isClosed;
        }

    }

}
