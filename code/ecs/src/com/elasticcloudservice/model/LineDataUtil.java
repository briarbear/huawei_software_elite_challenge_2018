package com.elasticcloudservice.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LineDataUtil {
    private static LineDataUtil instance;
    private String flavor;
    private LocalDate date;


    private LineDataUtil(String flavor,LocalDate date){
        this.flavor = flavor;
        this.date = date;
    }
    public static LineDataUtil getInstance(String lineData){

        String[] strings = lineData.split("\t");
        String sflavor = strings[1];

        String[] ss = strings[2].split(" ");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate sdate = LocalDate.parse(ss[0],dtf);


        if (instance == null){
            instance = new LineDataUtil(sflavor,sdate);

        }else {

            instance.flavor = sflavor;
            instance.date = sdate;
        }

        return instance;
    }


    public String getFlavor() {
        return flavor;
    }

    public LocalDate getDate() {
        return date;
    }
}
