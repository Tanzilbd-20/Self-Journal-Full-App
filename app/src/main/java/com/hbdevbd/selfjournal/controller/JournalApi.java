package com.hbdevbd.selfjournal.controller;

import android.app.Application;
import android.content.Context;

public class JournalApi extends Application {

    private static JournalApi INSTANCE;
    private String userName;
    private String userId;

    public JournalApi(){

    }

    public static JournalApi getINSTANCE(){
        if (INSTANCE == null) {
            INSTANCE = new JournalApi();
        }
        return INSTANCE;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }
    public String getUserName(){
        return userName;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }
    public String getUserId(){
        return userId;
    }


}
