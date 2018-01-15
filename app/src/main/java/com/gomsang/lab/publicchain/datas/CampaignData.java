package com.gomsang.lab.publicchain.datas;

/**
 * Created by Gyeongrok Kim on 2017-08-09.
 */

public class CampaignData {

    private String uuid;
    private String author;
    private Double latitude;
    private Double longitude;

    private String name;
    private String desc;
    private int goalOfSignature;
    private String attachImage;

    private boolean isFunding;
    private double goalOfContribution;

    private long signTime;

    private boolean isInstantCampaign = false;

    public CampaignData() {
    }

    public long getSignTime() {
        return signTime;
    }

    public void setSignTime(long signTime) {
        this.signTime = signTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getGoalOfSignature() {
        return goalOfSignature;
    }

    public void setGoalOfSignature(int goalOfSignature) {
        this.goalOfSignature = goalOfSignature;
    }

    public String getAttachImage() {
        return attachImage;
    }

    public void setAttachImage(String attachImage) {
        this.attachImage = attachImage;
    }

    public boolean isFunding() {
        return isFunding;
    }

    public void setFunding(boolean funding) {
        isFunding = funding;
    }

    public double getGoalOfContribution() {
        return goalOfContribution;
    }

    public void setGoalOfContribution(double goalOfContribution) {
        this.goalOfContribution = goalOfContribution;
    }

    public boolean isInstantCampaign() {
        return isInstantCampaign;
    }

    public void setInstantCampaign(boolean instantCampaign) {
        isInstantCampaign = instantCampaign;
    }
}
