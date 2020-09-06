package com.example.weather.weatherprovider.openweatherorg.findWeatherModel;

public class FindData {
    private String message;
    //Request code
    private int cod;
    private int count;
    private FindWeatherData[] list;

    public String getMessage() {
        return message;
    }

    public int getCod() {
        return cod;
    }

    public int getCount() {
        return count;
    }

    public FindWeatherData[] getList() {
        return list;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setList(FindWeatherData[] list) {
        this.list = list;
    }
}
