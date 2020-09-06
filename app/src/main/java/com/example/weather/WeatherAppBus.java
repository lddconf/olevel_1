package com.example.weather;

import org.greenrobot.eventbus.EventBus;

public class WeatherAppBus {
    private static EventBus bus;

    public static EventBus getBus() {
        if ( bus == null ) {
            bus = new EventBus();
        }
        return bus;
    }
}
