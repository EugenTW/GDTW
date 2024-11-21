package com.GDTW.dailystatistic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartDataDTO {

    private Map<String, Map<String, List<Integer>>> data;

    public ChartDataDTO() {
        data = new HashMap<>();
        data.put("created", new HashMap<>());
        data.put("used", new HashMap<>());
        data.get("created").put("url", new ArrayList<>());
        data.get("created").put("album", new ArrayList<>());
        data.get("created").put("image", new ArrayList<>());
        data.get("used").put("url", new ArrayList<>());
        data.get("used").put("album", new ArrayList<>());
        data.get("used").put("image", new ArrayList<>());
    }

    public Map<String, Map<String, List<Integer>>> getData() {
        return data;
    }

    public List<Integer> getCreatedData(String type) {
        return data.get("created").get(type);
    }

    public List<Integer> getUsedData(String type) {
        return data.get("used").get(type);
    }

    public void addCreatedData(String type, Integer value) {
        data.get("created").get(type).add(value);
    }

    public void addUsedData(String type, Integer value) {
        data.get("used").get(type).add(value);
    }
}
