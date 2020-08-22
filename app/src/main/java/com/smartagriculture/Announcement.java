package com.smartagriculture;

public class Announcement {

    private String announcement;
    private long postedTime, endTime;


    public Announcement(){

    }

    public Announcement(String announcement, long postedTime, long endTime) {
        this.announcement = announcement;
        this.postedTime = postedTime;
        this.endTime = endTime;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public long getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(long postedTime) {
        this.postedTime = postedTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
