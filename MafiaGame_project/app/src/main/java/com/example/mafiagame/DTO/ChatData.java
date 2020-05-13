package com.example.mafiagame.DTO;

import java.io.Serializable;

public class ChatData implements Serializable {

    private String message;
    private String nickName;
    private boolean isLottie;
    private int lottieNum;

    public ChatData(){}

    public ChatData(String nickName,String message,int lottieNum, boolean isLottie) {
        this.nickName = nickName;
        this.message = message;
        this.isLottie = isLottie;
        this.lottieNum = lottieNum;
    }

    public ChatData(String nickName,String message) {
        this.nickName = nickName;
        this.message = message;
        this.isLottie = false;
        this.lottieNum = 0;
    }

    public ChatData(String nickName, int lottieNum) {
        this.nickName = nickName;
        this.message = "";
        this.isLottie = true;
        this.lottieNum = lottieNum;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setLottie(boolean isLottie) { this.isLottie = isLottie; }
    public void setLottieNum(int lottieNum) { this.lottieNum = lottieNum; }

    public String getNickName() {
        if (nickName == null) return "";
        else return nickName;
    }
    public String getMessage() {
        if (message == null) return "";
        else return message;
    }
    public boolean isLottie() {
        return isLottie;
    }
    public int getLottieNum() {
        return lottieNum;
    }
}