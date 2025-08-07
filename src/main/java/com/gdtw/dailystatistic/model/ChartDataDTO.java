package com.gdtw.dailystatistic.model;

import java.io.Serializable;
import java.util.*;

public class ChartDataDTO implements Serializable {

    private static final String CREATED = "created";
    private static final String USED = "used";
    private static final String URL = "url";
    private static final String ALBUM = "album";
    private static final String IMAGE = "image";

    private final Map<String, Map<String, List<Integer>>> data;

    public ChartDataDTO() {
        data = new HashMap<>();
        data.put(CREATED, new HashMap<>());
        data.put(USED, new HashMap<>());

        data.get(CREATED).put(URL, new ArrayList<>());
        data.get(CREATED).put(ALBUM, new ArrayList<>());
        data.get(CREATED).put(IMAGE, new ArrayList<>());

        data.get(USED).put(URL, new ArrayList<>());
        data.get(USED).put(ALBUM, new ArrayList<>());
        data.get(USED).put(IMAGE, new ArrayList<>());
    }

    public Map<String, Map<String, List<Integer>>> getData() {
        return data;
    }

    public List<Integer> getCreatedData(String type) {
        return data.get(CREATED).get(type);
    }

    public List<Integer> getUsedData(String type) {
        return data.get(USED).get(type);
    }

    public void addCreatedData(String type, Integer value) {
        data.get(CREATED).get(type).add(value);
    }

    public void addUsedData(String type, Integer value) {
        data.get(USED).get(type).add(value);
    }

}
