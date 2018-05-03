package com.elasticcloudservice.util;

public final class Noise {


    /**
     * 求取数组非零元素的平均值，
     * @param n
     * @param len
     * @return
     */
    public static  double average(int[] n,int len){

        int sum = 0,num = 0;

        for (int i = 0; i < len; i++) {
            if (n[i]!=0){
                num++;
                sum += n[i];
            }
        }

        double average = sum/num;
        //然后执行降噪处理
//        for (int i = 0; i < len; i++) {
//            if ((n[i] - 4*average)>0){
//                n[i] = 0;
//            }
//        }


        //数学降噪处理
        double sd = 0.0,variance = 0.0,sum2 = 0.0; //标准差 //方差

        for (int i = 0; i < len; i++) {
            if (n[i] !=0)
                sum2 += Math.pow((n[i]-average),2);
        }

        variance = sum2 * 1.0 / len;
        sd = Math.sqrt(variance);  //标准差

        //降噪
        for (int i = 0; i < len; i++) {

            if (n[i] != 0){
                if (n[i]>(5*sd+average)||n[i]<(average-5*sd)){
                    n[i] = 0;
                }
            }
        }

        //再计算均值
        sum = 0;num = 0;

        for (int i = 0; i < len; i++) {
            if (n[i]!=0){
                num++;
                sum += n[i];
            }
        }
        return sum/num;

    }
}
