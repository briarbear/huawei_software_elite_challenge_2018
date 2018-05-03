package com.elasticcloudservice.predict;


import com.elasticcloudservice.model.Flavor;
import com.elasticcloudservice.model.PhysicalM;

import java.util.*;

/**
 * 放置虚拟机
 */
public class PlaceVM {


    /**
     * 对指定的物理机总规格要求，提出放置方案，使得最高的使用率
     *
     * @param physicalMachine 物理机规格，仅此一种
     * @param flavors         虚拟机的对象集合
     * @param opt             优化的对象，0标识CPU，1标识内存
     * @return
     */
    public static List<String> place(String physicalMachine, List<Flavor> flavors, int opt, int vmNumber) {

        String[] pInforms = physicalMachine.split(" ");

        int pTotalCore = Integer.valueOf(pInforms[0]);        //物理机规格，核数
        int pTotalMem = Integer.valueOf(pInforms[1]) * 1024;  //换算单位  内存

        PhysicalM physicalM = new PhysicalM(pTotalCore, pTotalMem);  //物理机规格


        //放置虚拟机  --算法1
//        return placeAlgorithm(flavors, physicalM, opt, vmNumber);

        //放置虚拟机 ---算法2  效果太差，13分 暂时不予考虑
//        return placeAlgorithm2(flavors,physicalM,opt,vmNumber);

        //放置虚拟机 ---算法3  一个阀值，cpu，内存都优化
        return placeAlgorithm3(flavors,physicalM,opt,vmNumber);


    }


    /**
     * 放置算法1
     *
     * @param flavors
     * @param physicalM
     * @param opt
     */
    public static List<String> placeAlgorithm(List<Flavor> flavors, PhysicalM physicalM, int opt, int vmNumber) {

        String[] resultArray = new String[vmNumber + 1];  //假设物理机最小部署一台虚拟机
        Map<Flavor, Integer> map = new HashMap<>();
        int pTotalCore = physicalM.getCpuCore();
        int pTotalMem = physicalM.getMem();
        int cpu = 0, mem = 0;  //初始的物理机占用情况
        double valve = 0.6;   //阀值 0.8 0.7   0.6效果为188台
        int pn = 1;  //记录物理机的数量

        //对虚拟机排序
        flavorSort2(flavors, opt);

        //选择优化的对象


        int p = 1;  //倒数的游标

        while (!flavors.isEmpty()) { //如果虚拟机不为空，则继续操作


            double kc = opt == 0 ? cpu * 1.0 / pTotalCore : mem * 1.0 / pTotalMem;

            if (kc < valve) {  //继续放置高cpu的虚拟机

                Flavor f = flavors.get(0);
                String s1 = f.getName();
                int number = f.getNumber();
                if (number == 0) {
                    flavors.remove(0);
                    continue;
                }

                cpu += f.getCpuCore();
                mem += f.getMem();

                if (cpu <= pTotalCore && mem <= pTotalMem) {  //资源未溢出
                    //放置 先判断是否放置了flavor
                    if (map.containsKey(f)) {
                        map.put(f, map.get(f) + 1);
                        flavors.get(0).setNumber(number - 1);
                    } else {
                        map.put(f, 1);
                        flavors.get(0).setNumber(number - 1);
                    }

                } else {
                    //先保存map中的结果到result中
                    resultArray[pn] = String.valueOf(pn);
                    Iterator<Flavor> iterator = map.keySet().iterator();
                    while (iterator.hasNext()) {
                        Flavor ftemp = iterator.next();
                        resultArray[pn] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
                    }

                    //然后重置cpu mem
                    cpu = 0;
                    mem = 0;
                    pn++;
                    map.clear();
                    p = 1;

                }


            } else {  //补充分配高内存虚拟机

                Flavor f = flavors.get(flavors.size() - p);   //获取list中最后一个
                int number = f.getNumber();

                if (number == 0) {
                    //该型号虚拟机分类完，则从list中删除
                    flavors.remove(flavors.size() - p);
                    p = 1;
                    continue;
                }

                cpu += f.getCpuCore();
                mem += f.getMem();

                //判断资源是否溢出
                if (cpu <= pTotalCore && mem <= pTotalMem) {
                    //如果没有溢出

                    if (map.containsKey(f)) {
                        map.put(f, map.get(f) + 1);
                        flavors.get(flavors.size() - p).setNumber(number - 1);
                    } else {
                        map.put(f, 1);
                        flavors.get(flavors.size() - p).setNumber(number - 1);
                    }


                } else {
                    //再判断是否取到第一个
                    if (p == flavors.size()) {
                        //新建物理机，保存现有分配结果
                        cpu -= f.getCpuCore();
                        mem -= f.getMem();

                        //先保存map中的结果到result中
                        resultArray[pn] = String.valueOf(pn);

                        Iterator<Flavor> iterator = map.keySet().iterator();
                        while (iterator.hasNext()) {
                            Flavor ftemp = iterator.next();
                            resultArray[pn] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
                        }


                        //然后重置cpu mem
                        cpu = 0;
                        mem = 0;
                        pn++;
                        map.clear();
                        p = 1;

                    } else {
                        cpu -= f.getCpuCore();
                        mem -= f.getMem();
                        p++;   //倒数游标后退一个

                    }
                }

            }


        }

        //再将map中的放置到新物理机中
        if (!map.isEmpty()) {
            resultArray[pn] = String.valueOf(pn);

            Iterator<Flavor> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                Flavor ftemp = iterator.next();
                resultArray[pn] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
            }

            //然后重置cpu mem
            cpu = 0;
            mem = 0;
            pn++;
            map.clear();
            p = 1;
        }

        resultArray[0] = String.valueOf(pn - 1);
        List<String> resultList = Arrays.asList(resultArray);  //将数组转为列表

        return resultList;
    }

    /**
     * 放置算法2
     *
     * @param flavors
     * @param physicalM
     * @param opt
     */
    public static List<String> placeAlgorithm2(List<Flavor> flavors, PhysicalM physicalM, int opt, int vmNumber) {

        //逐层优化算法
        double[] value = new double[]{0.2, 0.5};   //两层优化 0.2-0.5:171  0.1-1.4:170
        double occupancy = 0.0;
        int cpuTotal = physicalM.getCpuCore(), memTotal = physicalM.getMem();
        String[] resultArray = new String[vmNumber + 1];
        int index = 0, cpu = 0, mem = 0, point = 1;
        Map<Flavor, Integer> map = new HashMap<>();
        //1. 对虚拟机排序 高占用靠前
        flavorSort(flavors, opt);

        //2. 逐层优化
        while (!flavors.isEmpty()) {
            occupancy = opt == 0 ? cpu * 1.0 / cpuTotal : mem * 1.0 / memTotal;

            if (occupancy < value[0]) {  //满足第一层要求
                Flavor f = flavors.get(index);
                String name = f.getName();
                int number = f.getNumber();
                if (number == 0) {  //如果该虚拟机分配完毕，则delete
                    flavors.remove(index);
                    if (index == flavors.size())
                        index--;
                    continue;
                }

                cpu += f.getCpuCore();
                mem += f.getMem();

                // if voer the limit 2
                occupancy = opt == 0 ? cpu * 1.0 / cpuTotal : mem * 1.0 / memTotal;
                if (occupancy < value[1]) {  //如果没找到 第二层阀值，则放进去map
                    if (map.containsKey(f)) {
                        map.put(f, map.get(f) + 1);
                        flavors.get(index).setNumber(number - 1);
                    } else {
                        map.put(f, 1);
                        flavors.get(index).setNumber(number - 1);
                    }

                    //加入之后是否满足第一个阈值
                    occupancy = opt == 0 ? cpu * 1.0 / cpuTotal : mem * 1.0 / memTotal;
                    if (occupancy >= value[0] && index < flavors.size() - 1) index++;
                } else {
                    //超过了第二个阀值
                    cpu -= f.getCpuCore();
                    mem -= f.getMem();
                    index++;
                }

            } else { //不满足第一个阀值

                //如果某个参数，如cpu，或者内存已经满了，则同样可以停止下来
                if (cpu == cpuTotal || mem == memTotal) {
                    resultArray[point] = String.valueOf(point);
                    Iterator<Flavor> iterator = map.keySet().iterator();
                    while (iterator.hasNext()) {
                        Flavor ftemp = iterator.next();
                        resultArray[point] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
                    }
//                    resultArray[point] += " " + (int)(occupancy*100) + "%";
                    //重置cpu，men
                    cpu = 0;
                    mem = 0;
                    point++;
                    map.clear();
                    index = 0;
                    continue;
                }

//                if (index == flavors.size())  //是否取到最后一个 可能有问题，待修改
//                    break;

                if (occupancy < value[1]) { //满足第二个阀值


                    Flavor f = flavors.get(index);
                    int number = f.getNumber();
                    if (number == 0) {
                        flavors.remove(f);
                        if (index == flavors.size())
                            index--;
                        continue;
                    }

                    cpu += f.getCpuCore();
                    mem += f.getMem();

                    //判断是否满足总资源要求
                    if (cpu <= cpuTotal && mem <= memTotal) {
                        //满足资源要求
                        if (map.containsKey(f)) {
                            map.put(f, map.get(f) + 1);
                            flavors.get(index).setNumber(number - 1);
                        } else {
                            map.put(f, 1);
                            flavors.get(index).setNumber(number - 1);
                        }

                        //再判断加入之后是否还满足第二个阀值
                        occupancy = opt == 0 ? cpu * 1.0 / cpuTotal : mem * 1.0 / memTotal;
                        if (occupancy >= value[1] && index < flavors.size() - 1)
                            index++;
                    } else {
                        //不满足总资源要求
                        //判断是否到最后一个
                        if (index == flavors.size()) {
                            resultArray[point] = String.valueOf(point);
                            Iterator<Flavor> iterator = map.keySet().iterator();
                            while (iterator.hasNext()) {
                                Flavor ftemp = iterator.next();
                                resultArray[point] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
                            }
//                            resultArray[point] += " " + (int)(occupancy*100) + "%";
                            //重置cpu，men
                            cpu = 0;
                            mem = 0;
                            point++;
                            map.clear();
                            index = 0;
                        }

                        index++;  //尝试下一个
                        cpu -= f.getCpuCore();
                        mem -= f.getMem();
                    }

                } else {
                    //先判断是否满足总资源要求
                    if (cpu <= cpuTotal && mem <= memTotal) {
                        //再取一个
                        Flavor f = flavors.get(index);
                        int number = f.getNumber();
                        if (number == 0) {
                            flavors.remove(f);
                            if (index == flavors.size())
                                index--;
                            continue;
                        }

                        cpu += f.getCpuCore();
                        mem += f.getMem();

                        //再判断加入之后是否满足总资源要求
                        if (cpu <= cpuTotal && mem <= memTotal) {
                            //满足资源要求
                            if (map.containsKey(f)) {
                                map.put(f, map.get(f) + 1);
                                flavors.get(index).setNumber(number - 1);
                            } else {
                                map.put(f, 1);
                                flavors.get(index).setNumber(number - 1);
                            }
                        } else {

                            //不满足总资源要求
                            //判断是否到最后一个
                            if (index == flavors.size()) {
                                resultArray[point] = String.valueOf(point);
                                Iterator<Flavor> iterator = map.keySet().iterator();
                                while (iterator.hasNext()) {
                                    Flavor ftemp = iterator.next();
                                    resultArray[point] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
                                }
//                                resultArray[point] += " " + (int)(occupancy*100) + "%";
                                //重置cpu，men
                                cpu = 0;
                                mem = 0;
                                point++;
                                map.clear();
                                index = 0;
                            }

                            index++;  //尝试下一个
                            cpu -= f.getCpuCore();
                            mem -= f.getMem();
                        }
                    }
                }

            }
        }

        //再将map中的放置到新物理机中
        if (!map.isEmpty()) {
            resultArray[point] = String.valueOf(point);

            Iterator<Flavor> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                Flavor ftemp = iterator.next();
                resultArray[point] += " " + ftemp.getName() + " " + String.valueOf(map.get(ftemp));
            }
//            resultArray[point] += " " + (int)(occupancy*100) + "%";
            //然后重置cpu mem
            cpu = 0;
            mem = 0;
            point++;
            map.clear();
            index = 0;
        }
        resultArray[0] = String.valueOf(point - 1);

        //加上百分比利用率


        List<String> resultList = Arrays.asList(resultArray);  //将数组转为列表


        return resultList;
    }

    /**
     * 放置算法3，同时对CPU与内存优化
     *
     * @param flavors
     * @param physicalM
     * @param opt
     * @param vmNumber
     * @return
     */
    public static List<String> placeAlgorithm3(List<Flavor> flavors, PhysicalM physicalM, int opt, int vmNumber) {
        //同时对CPU与内存优化
        double value = 0.5,value2 = 0.65;  //如果CPU或者内存占用超过该比例，就跳过 0.5 0.6 78.998：第二个参数调整到0.65没变化
        double cpuOcy = 0.0, memOcy = 0.0;  //内存与CPU占用比例
        int cpuTotal = physicalM.getCpuCore(), memTotal = physicalM.getMem();
        String[] result = new String[vmNumber + 1];
        int index = 0, cpu = 0, mem = 0, point = 1;
        Map<String, Integer> map = new HashMap<>(15); //初始容量大小

        //1. 对虚拟机排序
        flavorSort3(flavors, opt);  //按照特定次序排序
//        flavorSort(flavors, opt);  //
//        flavorSort2(flavors, opt);  //

        //2. 逐个放置
        while (!flavors.isEmpty()) {
            cpuOcy = cpu * 1.0 / cpuTotal;
            memOcy = (mem * 1.0) / memTotal;
            if (cpuOcy < value && memOcy < value) {
                Flavor f = flavors.get(index);
                int n = f.getNumber();
                if (n == 0) {  //判断数量
                    flavors.remove(index);
                    if (index == flavors.size()) index--;
                    continue;
                }

                cpu += f.getCpuCore();
                mem += f.getMem();

                if (cpu <= cpuTotal && mem <= memTotal){
                    String name = f.getName();
                    if (map.containsKey(name)){
                        map.put(name,map.get(name)+1);
                        flavors.get(index).setNumber(n-1);
                    }else {
                        map.put(name,1);
                        flavors.get(index).setNumber(n-1);
                    }

                    cpuOcy = cpu * 1.0 / cpuTotal;
                    memOcy = (mem * 1.0) / memTotal;
                    if (cpuOcy < value && memOcy < value)
                        continue;
                    else if (index != flavors.size()-1)
                                index++;


                }else {
                    //不放入
                    cpu -= f.getCpuCore();
                    mem -= f.getMem();

                    //判断是否已经取到最后一个了
                    if (index == flavors.size()-1){
                        //如果已经是最后一个，则保存结果到result
                        result[point] = String.valueOf(point);
                        Iterator<String> iterator = map.keySet().iterator();
                        while (iterator.hasNext()){
                            String sName = iterator.next();
                            result[point] += " " + sName + " " + map.get(sName);
                        }

                        //重置
                        cpu = 0;mem = 0;point++;map.clear();index = 0;
                    }else {
                        //如果不是最后一个，则继续往下走
                        index++;
                    }
                }

            }else {
                //已经满足了第一个条件，再判断是否满足第二个条件
                if (cpuOcy < value2 && memOcy < value2){

                }else {
                    if (!(index == flavors.size()-1)) index++;  //如果不是最后一个，继续往下
                }

                Flavor f = flavors.get(index);
                int n = f.getNumber();
                if (n == 0) {  //判断数量
                    flavors.remove(index);
                    if (index == flavors.size()) index--;
                    continue;
                }

                cpu += f.getCpuCore();
                mem += f.getMem();

                if (cpu <= cpuTotal && mem <= memTotal){
                    String name = f.getName();
                    if (map.containsKey(name)){
                        map.put(name,map.get(name)+1);
                        flavors.get(index).setNumber(n-1);
                    }else {
                        map.put(name,1);
                        flavors.get(index).setNumber(n-1);
                    }

                    //放入后再判断是否index++
                    cpuOcy = cpu * 1.0 / cpuTotal;
                    memOcy = (mem * 1.0) / memTotal;
                    if (cpuOcy < value2 && memOcy < value2)
                        continue;
                    else if (index != flavors.size()-1)
                        index++;


                }else {
                    //不放入
                    cpu -= f.getCpuCore();
                    mem -= f.getMem();

                    //判断cpu或者men满了，停止下来
                    if (cpu==cpuTotal || mem == memTotal){
                        result[point] = String.valueOf(point);
                        Iterator<String> iterator = map.keySet().iterator();
                        while (iterator.hasNext()){
                            String sName = iterator.next();
                            result[point] += " " + sName + " " + map.get(sName);
                        }

                        //重置
                        cpu = 0;mem = 0;point++;map.clear();index=0;
                        continue;
                    }
                    //判断是否已经取到最后一个了
                    if (index == flavors.size()-1){
                        //如果已经是最后一个，则保存结果到result
                        result[point] = String.valueOf(point);
                        Iterator<String> iterator = map.keySet().iterator();
                        while (iterator.hasNext()){
                            String sName = iterator.next();
                            result[point] += " " + sName + " " + map.get(sName);
                        }

                        //重置
                        cpu = 0;mem = 0;point++;map.clear();index=0;
                    }else {
                        //如果不是最后一个，则继续往下走
                        index++;
                    }
                }

            }
        }


        //map是否为空,
        if (!map.isEmpty()){
            result[point] = String.valueOf(point);
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()){
                String sName = iterator.next();
                result[point] += " " + sName + " " + map.get(sName);
            }

            //重置
            cpu = 0;mem = 0;point++;map.clear();
        }

        result[0]  = String.valueOf(point-1);
        return Arrays.asList(result);

    }

    public static void flavorSort2(List<Flavor> flavors, int opt) {

        if (opt == 0) {
            //对Flavor对象集合排序 内存占用比（斜率）越小，排序越靠前
            flavors.sort((lf, rf) -> {
                if (lf.getSlope() - rf.getSlope() == 0) {
                    return lf.getMem() - rf.getMem();
                } else {
                    return lf.getSlope() > rf.getSlope() ? 1 : -1;
                }
            });
        } else {
            //优化内存的情况，倒序排序，内存占用比，越高越靠前
            flavors.sort((lf, rf) -> {
                if (lf.getSlope() - rf.getSlope() == 0) {
                    return rf.getMem() - lf.getMem();
                } else {
                    return lf.getSlope() > rf.getSlope() ? -1 : 1;
                }
            });
        }
    }

    public static void flavorSort(List<Flavor> flavors, int opt) {

        //opt 优化参数 0优化CPU，1优化mem
        if (opt == 0) {
            //cpu越大，越靠前
            flavors.sort((lf, rf) -> {
                if (lf.getCpuCore() == rf.getCpuCore())
                    return rf.getMem() - lf.getMem();
                else
                    return lf.getCpuCore() < rf.getCpuCore() ? 1 : -1;
            });
        } else {
            //内存占用越大，越靠前
            flavors.sort((lf, rf) -> {
                if (lf.getMem() == rf.getMem())
                    return rf.getCpuCore() - lf.getCpuCore();
                else
                    return lf.getMem() < rf.getMem() ? 1 : -1;
            });
        }

    }

    public static void flavorSort3(List<Flavor> flavors, int opt) {

        //按照15 13 14 12 10 11 9 7 8 6 4 5 3 1 2排序
        List<String> sortflavor = new ArrayList<>();
        sortflavor.add("flavor15");
        sortflavor.add("flavor13");
        sortflavor.add("flavor14");
        sortflavor.add("flavor12");
        sortflavor.add("flavor10");
        sortflavor.add("flavor11");
        sortflavor.add("flavor9");
        sortflavor.add("flavor7");
        sortflavor.add("flavor8");
        sortflavor.add("flavor6");
        sortflavor.add("flavor4");
        sortflavor.add("flavor5");
        sortflavor.add("flavor3");
        sortflavor.add("flavor1");
        sortflavor.add("flavor2");

        flavors.sort((l, r) -> {
//            return sortflavor.indexOf(l.getName()) - sortflavor.indexOf(r.getName());
            int leftIndex = sortflavor.indexOf(l.getName());
            int rightIndex = sortflavor.indexOf(r.getName());

            return leftIndex - rightIndex;
        });

    }

    public static void groupFlavor(List<Flavor> flavors, List<Flavor> first, List<Flavor> second, List<Flavor> third) {
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        Set<String> set3 = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            String s1 = "flavor" + (i * 3 + 3);
            String s2 = "flavor" + (i * 3 + 1);
            String s3 = "flavor" + (i * 3 + 2);
            set1.add(s1);
            set2.add(s2);
            set3.add(s3);
        }

        for (Flavor flavor : flavors) {
            String name = flavor.getName();
            if (set1.contains(name))
                first.add(flavor);
            else if (set2.contains(name))
                second.add(flavor);
            else if (set3.contains(name))
                third.add(flavor);
        }
    }


}
