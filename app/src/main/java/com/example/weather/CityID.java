package com.example.weather;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class CityID implements Serializable {
    private String name;
    private String country;
    private int id;

    public CityID(String name, String country, int id) {
        this.name = name;
        this.country = country;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if ( obj instanceof CityID ) {
            CityID rval = (CityID)obj;
            if ( rval == null ) {
                return false;
            }
            return rval.id == this.id;
        }
        return false;
    }
}
