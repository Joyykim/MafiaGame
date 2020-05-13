package com.example.mafiagame.DTO;

public class LobbyData {

    private String roomName;
    private String roomID;
    private int prsnl;

    public LobbyData(){}

    public LobbyData(String roomName, String roomID, int prsnl){
        this.roomName = roomName;
        this.roomID = roomID;
        this.prsnl = prsnl;
    }

    public void setRoomID(String roomID) { this.roomID = roomID; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public void setPrsnl(int prsnl) { this.prsnl = prsnl;}

    public String getRoomID() { return roomID; }
    public String getRoomName() { return roomName; }
    public int getPrsnl() { return prsnl; }
}
