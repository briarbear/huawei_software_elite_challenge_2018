package com.elasticcloudservice.model;


/**
 * 虚拟机对象
 */
public class Flavor {

    private String name;  //虚拟机名称
    private int cpuCore;  //cpu核数
    private int mem;      //内存大小
    private int number;   //该规则的虚拟机数量
    private double slope;  //men / cpu 斜率大小

    public Flavor() {
    }

    public Flavor(String name, int cpuCore, int mem, int number) {
        this.name = name;
        this.cpuCore = cpuCore;
        this.mem = mem;
        this.slope = mem / cpuCore;
        this.number = number;
    }


    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public double getSlope() {
        return slope;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCpuCore(int cpuCore) {
        this.cpuCore = cpuCore;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public String getName() {
        return name;
    }



    public int getCpuCore() {
        return cpuCore;
    }



    public int getMem() {
        return mem;
    }



}
