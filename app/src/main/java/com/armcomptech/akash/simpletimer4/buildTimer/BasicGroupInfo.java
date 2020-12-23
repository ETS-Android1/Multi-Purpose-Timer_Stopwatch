package com.armcomptech.akash.simpletimer4.buildTimer;

import java.util.ArrayList;

public class BasicGroupInfo {
    ArrayList<BasicTimerInfo> basicTimerInfoArrayList;
    int repeatSets;
    String groupName;

    public BasicGroupInfo(ArrayList<BasicTimerInfo> basicTimerInfoArrayList, int repeatSets, String groupName) {
        this.basicTimerInfoArrayList = basicTimerInfoArrayList;
        this.repeatSets = repeatSets;
        this.groupName = groupName;
    }
}
