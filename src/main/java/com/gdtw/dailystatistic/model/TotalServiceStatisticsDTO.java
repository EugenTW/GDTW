package com.gdtw.dailystatistic.model;

import java.io.Serializable;

public class TotalServiceStatisticsDTO implements Serializable {
    private int totalShortUrlsCreated = 0;
    private int totalShortUrlsUsed = 0;
    private int totalImagesCreated = 0;
    private int totalImagesVisited = 0;
    private int totalImageAlbumsCreated = 0;
    private int totalImageAlbumsVisited = 0;
    private int totalWebServiceCount = 0;
    private int totalCssJsMinified = 0;
    private int totalWebpConverted = 0;

    public int getTotalShortUrlsCreated() {
        return totalShortUrlsCreated;
    }

    public void setTotalShortUrlsCreated(int totalShortUrlsCreated) {
        this.totalShortUrlsCreated = totalShortUrlsCreated;
    }

    public int getTotalShortUrlsUsed() {
        return totalShortUrlsUsed;
    }

    public void setTotalShortUrlsUsed(int totalShortUrlsUsed) {
        this.totalShortUrlsUsed = totalShortUrlsUsed;
    }

    public int getTotalImagesCreated() {
        return totalImagesCreated;
    }

    public void setTotalImagesCreated(int totalImagesCreated) {
        this.totalImagesCreated = totalImagesCreated;
    }

    public int getTotalImagesVisited() {
        return totalImagesVisited;
    }

    public void setTotalImagesVisited(int totalImagesVisited) {
        this.totalImagesVisited = totalImagesVisited;
    }

    public int getTotalImageAlbumsCreated() {
        return totalImageAlbumsCreated;
    }

    public void setTotalImageAlbumsCreated(int totalImageAlbumsCreated) {
        this.totalImageAlbumsCreated = totalImageAlbumsCreated;
    }

    public int getTotalImageAlbumsVisited() {
        return totalImageAlbumsVisited;
    }

    public void setTotalImageAlbumsVisited(int totalImageAlbumsVisited) {
        this.totalImageAlbumsVisited = totalImageAlbumsVisited;
    }

    public int getTotalWebServiceCount() {
        return totalWebServiceCount;
    }

    public void setTotalWebServiceCount(int totalWebServiceCount) {
        this.totalWebServiceCount = totalWebServiceCount;
    }

    public int getTotalCssJsMinified() {
        return totalCssJsMinified;
    }

    public void setTotalCssJsMinified(int totalCssJsMinified) {
        this.totalCssJsMinified = totalCssJsMinified;
    }

    public int getTotalWebpConverted() {
        return totalWebpConverted;
    }

    public void setTotalWebpConverted(int totalWebpConverted) {
        this.totalWebpConverted = totalWebpConverted;
    }

}

