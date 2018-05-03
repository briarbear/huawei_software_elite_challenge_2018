package com.elasticcloudservice.predict;

import com.elasticcloudservice.model.Flavor;
import com.elasticcloudservice.model.LineDataUtil;
import com.elasticcloudservice.util.Noise;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PredictFlavor {
    private static Map<LocalDate, Map<String, Integer>> map = new TreeMap<>();
    private static LocalDate startDate = null; //训练集结束的时间
    private static LocalDate endDate = null;  //训练集结束的时间
    private static LocalDate start = null;   //预测集开始的时间
    private static LocalDate end = null;     //预测集结束的时间
    private static Map<String,List<String>> param = new HashMap<>(15);
    private static Map<String,List<String>> param0 = new HashMap<>(15);

    public static String[] predict(String[] ecsContent, String[] inputContent) {

        //这是模型一的默认参数列表，需要逐个调整
        param0.put("flavor1",Arrays.asList("0.99","6"));  //再往上分数降低,0.985和0.99(优)相差0.086，0.987和0.99一样分数
        param0.put("flavor2",Arrays.asList("0.976","10"));   //暂时最优 0.98 分数81.211 0.976 分数81.219
        param0.put("flavor3",Arrays.asList("1","6"));    //没用
        param0.put("flavor4",Arrays.asList("1","3"));    //没用
        param0.put("flavor5",Arrays.asList("1","3"));    //1和3基本上最优
        param0.put("flavor6",Arrays.asList("1","3"));    //没用
        param0.put("flavor7",Arrays.asList("1","3"));   //没用
        param0.put("flavor8",Arrays.asList("0.995","3"));   //再往上分数降低
        param0.put("flavor9",Arrays.asList("1","3"));    //3往上降低分数,前面第一个参数怎么变都没用了0.94-1
        param0.put("flavor10",Arrays.asList("1","3"));   //没用
        param0.put("flavor11",Arrays.asList("1","3"));   //最优，10台左右
        param0.put("flavor12",Arrays.asList("1","3"));   //没用
        param0.put("flavor13",Arrays.asList("1","0"));   //没用
        param0.put("flavor14",Arrays.asList("1","0"));   //没用
        param0.put("flavor15",Arrays.asList("1","0"));   //没用


        //注意，这是模型三的参数列表，我这里将数组类型改了，参数输入的类型为字符串
        param.put("flavor1",Arrays.asList("6","2"));  //ok
        param.put("flavor2",Arrays.asList("8","3"));  //ok
        param.put("flavor3",Arrays.asList("2","3"));  //ok
        param.put("flavor4",Arrays.asList("2","3"));  //ok
        param.put("flavor5",Arrays.asList("6","1"));  //偏大
        param.put("flavor6",Arrays.asList("1","1"));
        param.put("flavor7",Arrays.asList("1","1"));
        param.put("flavor8",Arrays.asList("4","3"));  //ok
        param.put("flavor9",Arrays.asList("4","1")); //ok
        param.put("flavor10",Arrays.asList("1","1"));
        param.put("flavor11",Arrays.asList("1","1"));
        param.put("flavor12",Arrays.asList("1","1"));
        param.put("flavor13",Arrays.asList("1","1"));
        param.put("flavor14",Arrays.asList("1","1"));
        param.put("flavor15",Arrays.asList("2","1"));




        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int flavorNumber = Integer.valueOf(inputContent[2]);
        String optString = inputContent[flavorNumber + 4];  //优化选项
        String physicMachine = inputContent[0];    //物理机规格
        start = LocalDate.parse(inputContent[flavorNumber + 6].split(" ")[0], dtf);
        end = LocalDate.parse(inputContent[flavorNumber + 7].split(" ")[0], dtf);

        handle(ecsContent);

        Map<String, Integer> flavorResult = new TreeMap<>();
        //保存虚拟机规格列表
        List<Flavor> flavors = new ArrayList<>();
        //预测虚拟机数量
        for (int i = 3; i < flavorNumber + 3; i++) {
            String[] strings = inputContent[i].split(" ");
            String name = strings[0];
            int cpuCore = Integer.valueOf(strings[1]);
            int mem = Integer.valueOf(strings[2]);
            int number = predictNumber(name);  //预测函数1  这个是原始预测模型
//            int number = predictNumber2(name);  //预测函数2 均值预测 分数63.308 效果差，
//            int number = predictNumber3(name);
//            int number = (i*i + i + 2)%100;
//            int number = 100;
            Flavor f = new Flavor(name, cpuCore, mem, number);
            flavors.add(f);

        }

        int vmNumber = 0;  //预测的虚拟机总数
        System.out.println("xxx");
        String[] results = new String[ecsContent.length];   //训练数据

        int i = 1;
        for (; i <= flavors.size(); i++) {
            Flavor f = flavors.get(i - 1);
            results[i] = f.getName() + " " + f.getNumber();
            vmNumber += f.getNumber();
        }

        results[0] = String.valueOf(vmNumber);


        //至此，result中填充完预测结果，缺少放置结果

        optimization(results, flavorNumber,flavors); //可以考虑对results字符串数组进行预测优化



        int opt = 1;
        if (optString.equals("CPU"))
            opt = 0;
        opt = 0;  //只按CPU来优化
        List<String> placevmList = PlaceVM.place(physicMachine, flavors, opt, vmNumber); //放置虚拟机

        //此时i游标位置为results中下一个元素位置
        i++;
        results[i++] = "";
        for (int j = 0; j <= Integer.valueOf(placevmList.get(0)); j++) {
            results[j + i] = placevmList.get(j);
        }

        return results;
    }


    /**
     * 优化预测结果
     *
     * @param flavorNumber 虚拟机规格数量
     * @param results      虚拟机预测结果
     */
    public static void optimization(String[] results, int flavorNumber,List<Flavor> flavors) {

        List<Integer> flavor = new ArrayList<>();

        int sum = 0, count = flavorNumber;


        for (int i = 1; i <= count; i++) {
            int n = Integer.valueOf(results[i].split(" ")[1]);
            flavor.add(n);
            sum += n;
        }

        double average = sum * 1.0 / count; //均值

        //计算标准差
        double sd = 0.0, variance = 0.0, sum2 = 0.0; //标准差 //方差

        for (Integer integer : flavor) {
            sum2 += Math.pow(integer - average, 2);
        }
        variance = sum2 * 1.0 / count;
        sd = Math.sqrt(variance);  //标准差


        //优化
        int index = 0;
        for (Integer integer : flavor) {

            int param = 2;  //3 改成2 没差别
            double u = 0.2; //将成倍数变化
            if (integer > (param * sd + average)) {
//                int ceil = (int) Math.ceil(integer + average);
                int ceil = (int) Math.ceil(integer *(1+u));
                flavor.set(index, ceil);
            }
            if (integer < (average - param * sd)) {

//                int ceil = (int) Math.ceil(integer - average);
                int ceil = (int) Math.ceil(integer *(1-u));
                ceil = ceil < 0 ? 0 : ceil;
                flavor.set(index, ceil);
            }

            index++;
        }

        //再统计总虚拟机数量
        sum = 0;
        for (Integer integer : flavor) {
            sum += integer;
        }


        results[0] = String.valueOf(sum);
        for (int i = 1; i <= count; i++) {
            results[i] = results[i].split(" ")[0] + " " + String.valueOf(flavor.get(i - 1));
        }

        //对flavors更新
        index = 0;
        for (Flavor flavor1 : flavors) {
            flavor1.setNumber(flavor.get(index++));
        }

        return;
    }


    /**
     * 平均值预测模型
     * @param flavor
     * @return
     */
    public static int predictNumber2(String flavor){
        int size = map.size();    //训练数据集大小
        int res = 0;

        long daysDiff = ChronoUnit.DAYS.between(start, end);
        int dist = Integer.valueOf((int) daysDiff);  //预测集大小

        int[]  st = new int[size];

        //先根据训练集求出st数组前size个元素
        Iterator<LocalDate> iter = map.keySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Map<String, Integer> mapTemp = map.get(iter.next());
            Iterator<String> iterator = mapTemp.keySet().iterator();
            while (iterator.hasNext()) {
                String flavorKey = iterator.next();
                if (flavorKey.equals(flavor))
                    st[i] = mapTemp.get(flavorKey);
            }

            i++;
        }

        //对数组求和
        int sum = 0;
        for (int i1 : st) {
            sum += i1;
        }

        return (int) Math.ceil(sum * (dist*1.0/size)) + 3;
    }


    /**
     * 预测某个flavor的最终时间段的数量
     *
     * @param flavor
     * @return
     */
    public static int predictNumber(String flavor) {


        int size = map.size();    //训练数据集大小
        int res = 0;

        long daysDiff = ChronoUnit.DAYS.between(start, end);
        int dist = Integer.valueOf((int) daysDiff);  //预测集大小


        int[] st = new int[size];

        //先根据训练集求出st数组前size个元素
        Iterator<LocalDate> iter = map.keySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Map<String, Integer> mapTemp = map.get(iter.next());
            Iterator<String> iterator = mapTemp.keySet().iterator();
            while (iterator.hasNext()) {
                String flavorKey = iterator.next();
                if (flavorKey.equals(flavor))
                    st[i] = mapTemp.get(flavorKey);
            }

            i++;
        }


        //预测后续的flavor数量大小
        int temp = (int) Math.ceil((dist / 7) * logistic(st, size));
        List<String> pList = param0.get(flavor);
        double a = Double.valueOf(pList.get(0));
        int b = Integer.valueOf(pList.get(1));

        //相当于我这个算法的a=1，b=3 明天针对模型1调参的话，将下面的1替换为a，3替换为b
        //然后重新构建参数列表，a全为1，b全为3，然后逐个调整，过程类似我昨天那样，@何伟超
        res = (int) Math.ceil(temp * a+ b);  //这里我改好了，

        return res;
    }

    /**
     * 预测算法3 统计训练数据中7天一个时间段，某台虚拟机的数量，作为预测
     * @param flavor
     * @return
     */
    public static int predictNumber3(String flavor){
        //先求训练集大小
        int weekSize = 7;
        int size = map.size();
        int size2 = Integer.valueOf((int)ChronoUnit.DAYS.between(startDate,endDate));
        long daysDiff = ChronoUnit.DAYS.between(start, end);
        int dist = Integer.valueOf((int) daysDiff);  //预测集大小

        int trainSize = (int) Math.ceil((size2+1)*1.0/weekSize);
        //新建一个 数组，保存训练集某个虚拟机出现的次数
        int [] history = new int[trainSize];
        LocalDate[] dates = new LocalDate[trainSize+1];
        //填充时间区间数组
        for (int i = 0; i <= trainSize; i++) {
            if (i==trainSize)
                dates[i] = endDate;

            dates[i] = startDate.plusDays(i*weekSize);
        }

        //遍历map，填充history数组
        Iterator<LocalDate> iterator = map.keySet().iterator();
        int index = 0,count = 0;
        while (iterator.hasNext()){
            LocalDate date = iterator.next();
            if ((date.isAfter(dates[index])|| date.equals(dates[index])) && date.isBefore(dates[index+1])){
                Iterator<String> iter = map.get(date).keySet().iterator();
                while (iter.hasNext()){
                    String ftemp = iter.next();
                    if (ftemp.equals(flavor))
                        count += map.get(date).get(ftemp);
                }
            }else {
                index++;
                history[index-1] = count;
                count = 0;
                Iterator<String> iter = map.get(date).keySet().iterator();
                while (iter.hasNext()){
                    String ftemp = iter.next();
                    if (ftemp.equals(flavor))
                        count += map.get(date).get(ftemp);
                }
            }
        }

        if (count != 0)
            history[index] = count;

        //计算预测值
        int res = 0,len = history.length;
        double sum  = 0.0;
        List<String> pList = param.get(flavor);
        double a = Double.valueOf(pList.get(0));
        int b = Integer.valueOf(pList.get(1));
        for (int i = 0; i < len; i++) {
            sum +=  history[i] * i * a;
        }

        res = (int )Math.ceil(sum * 1.0 / (len*(len+1)));
        res = (int)Math.ceil((dist/weekSize)*res*1.0);

        return res+b;
    }

    /**
     * 根据预测模型，计算到pos天的flavor个数
     *
     * @param st  存储某个flavor的以时间为下标的个数数组
     * @param len 数组长度
     * @return
     */
    public static double logistic(int[] st, int len) {


        Double average = Noise.average(st, len);

        double sum = 0.0;
        //计算权重数组
        int[] weight = new int[len];
        for (int i = 0; i < len; i++) {
            //使用1,2,3,4,...权重模型
            weight[i] = 2 * (i + 1);
//            weight[i] = (int) (Math.pow(2,i)/(i+1));
        }

        for (int i = 0; i < len; i++) {
            sum += st[i] * weight[i];
        }

        sum = sum / (len * 5);


        return sum;
    }


    //解析训练数据时间
    public static void handle(String[] ecsContent) {



        for (int i = 0; i < ecsContent.length; i++) {

            String lineData = ecsContent[i];

            LineDataUtil util = LineDataUtil.getInstance(lineData);
            LocalDate date = util.getDate();
            if (i == ecsContent.length - 1)
                endDate = date;
            if (i == 0)
                startDate = date;

            String flavor = util.getFlavor();
            Map<String, Integer> si = new HashMap<>();
            if (map.containsKey(date)) {
                si = map.get(date);
                if (si.containsKey(flavor))
                    si.put(flavor, si.get(flavor) + 1);
                else
                    si.put(flavor, 1);

            } else {
                si.put(flavor, 1);
                map.put(date, si);
            }


        }


    }
}
