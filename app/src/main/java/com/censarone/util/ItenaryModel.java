package com.censarone.util;

import java.io.Serializable;

public class ItenaryModel implements Serializable {

    private Integer id;
    private String description;
    private String timeTaken;

    public ItenaryModel(Integer id,String description) {
        this.id = id;
        this.description = description;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
