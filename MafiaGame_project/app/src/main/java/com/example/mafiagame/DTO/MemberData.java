package com.example.mafiagame.DTO;

public class MemberData {

    private String nickName;
    private boolean alive;

    public MemberData(String nickName, boolean alive){
        this.alive = alive;
        this.nickName = nickName;
    }

    public MemberData(){}

    public String getNickName() { return nickName; }
    public boolean isAlive() {
        return alive;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }




}
