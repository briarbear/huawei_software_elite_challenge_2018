package com.elasticcloudservice.model;


import java.util.Map;

//物理机
public class PhysicalM {


    private int cpuCore;
    private int mem;
    private Map<Flavor,Integer> placeMap;


    public PhysicalM(int cpuCore, int mem) {
        this.cpuCore = cpuCore;
        this.mem = mem;
    }


    public int getCpuCore() {
        return cpuCore;
    }

    public void setCpuCore(int cpuCore) {
        this.cpuCore = cpuCore;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public Map<Flavor, Integer> getPlaceMap() {
        return placeMap;
    }

    public void setPlaceMap(Map<Flavor, Integer> placeMap) {
        this.placeMap = placeMap;
    }
}

