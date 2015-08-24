package com.example.ryosoga.myapplication;

/**
 * Created by ryosoga on 15/08/21.
 */
public class MyMqttConfig {
    public static final String TAG = "MQTT";

    private static final String ORG_ID = "mtd4jo";
    private static final String DEVICE_ID = "client1";
    private static final String DEVICE_TYPE = "client-android";
    private static final String EVENT_TOPIC_FORMAT = "iot-2/evt/telemetry/fmt/json";

    static final String USER = "use-token-auth";
    static final String PASSWORD = "OS_-(sw(2uWGq2vavL";
    static final String ENDPOINT_URL = "tcp://" + ORG_ID + ".messaging.internetofthings.ibmcloud.com:1883";
    static final String CLIENT_ID = "d:" + ORG_ID +":" + DEVICE_TYPE + ":" + DEVICE_ID;

    static String getEventTopic(String eventId) {
        return String.format(EVENT_TOPIC_FORMAT, eventId);
    }
}
