package com.example.mafiagame.DTO;

public class NickJobData {
    private String nickName;
    private String job;

    public NickJobData(String nickName, String job) {
        this.nickName = nickName;
        this.job = job;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
