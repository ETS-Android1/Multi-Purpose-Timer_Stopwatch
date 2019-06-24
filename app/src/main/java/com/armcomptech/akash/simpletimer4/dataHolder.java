package com.armcomptech.akash.simpletimer4;

public class dataHolder {
    private String data;
    private String testData;

    //getter
    public String getData() {return data;}
    public String getTestData() {return testData;}

    //setter
    public void setData(String data) {this.data = data;}
    public void setTestData(String testData) {this.testData = testData;}

    private static final dataHolder holder = new dataHolder();
    public static dataHolder getInstance() {return holder;}

    //make the data structure here
}
