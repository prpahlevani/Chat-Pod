package com.fanap.podchat.model;

public class MetaDataFile {
    private SdkFile sdk;
    private String user;


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SdkFile getSdk() {
        return sdk;
    }

    public void setSdk(SdkFile sdk) {
        this.sdk = sdk;
    }
}
