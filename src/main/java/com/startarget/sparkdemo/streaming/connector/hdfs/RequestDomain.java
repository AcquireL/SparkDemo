package com.startarget.sparkdemo.streaming.connector.hdfs;

public class RequestDomain {
    private String userid;
    private String url;
    private String ts;

    public RequestDomain() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public RequestDomain(String userid, String url, String ts) {
        this.userid = userid;
        this.url = url;
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "RequestDomain{" +
                "userid='" + userid + '\'' +
                ", url='" + url + '\'' +
                ", ts='" + ts + '\'' +
                '}';
    }
}
