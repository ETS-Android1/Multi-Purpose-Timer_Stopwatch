package com.armcomptech.akash.simpletimer4.buildTimer;

import java.util.ArrayList;

public class MasterInfo {
    ArrayList<BasicGroupInfo> basicGroupInfoArrayList;
    String masterName;

    MasterInfo(ArrayList<BasicGroupInfo> basicGroupInfoArrayList, String masterName) {
        this.basicGroupInfoArrayList = basicGroupInfoArrayList;
        this.masterName = masterName;
    }
}
