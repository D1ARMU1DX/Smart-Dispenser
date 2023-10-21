package com.example.test1;

public class HelperClass {
    String UID, encPass;

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getPassword() {
        return encPass;
    }

    public void setPassword(String password) {
        this.encPass = password;
    }

    public HelperClass(String UID, String encPass) {
        this.UID = UID;
        this.encPass = encPass;
    }
    public HelperClass(){

    }
}
