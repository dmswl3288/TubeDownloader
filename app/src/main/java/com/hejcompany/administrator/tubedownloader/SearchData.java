package com.hejcompany.administrator.tubedownloader;

/**
 * Created by Administrator on 2018-11-01.
 */

public class SearchData {
    String videoID;
    String title;
    String url;
    String publishedAt;
    String duration;

    // 생성자
    public SearchData(String videoID, String title, String url, String publishedAt, String duration)
    {
        super();
        this.videoID = videoID;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.duration = duration;
    }

    public String getVideoID()
    {
        return videoID;
    }

    public void setVideoID(String videoID)
    {
        this.videoID = videoID;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getPublishedAt()
    {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt)
    {
        this.publishedAt = publishedAt;
    }

    public String getDuration() { return duration; }  // 2019-02-18 동영상 길이 추가

    public void setDuration(String duration) { this.duration = duration; }
}
