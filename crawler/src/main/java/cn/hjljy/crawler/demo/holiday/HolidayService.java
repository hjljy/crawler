package cn.hjljy.crawler.demo.holiday;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 度假服务
 *
 * @author hjljy
 * @date 2021/07/27
 */
public class HolidayService {

    static List<String> holiday =new ArrayList<>();
    static List<String> extraWorkDay =new ArrayList<>();

    /**
     * 判断是否是工作日
     * @param time
     * @return
     */
    public static Boolean isWorkingDay(long time) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.of("+8"));
        String formatTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //是否加班日
        if(extraWorkDay.contains(formatTime)){
            return true;
        }
        //是否节假日
        if(holiday.contains(formatTime)){
            return false;
        }
        //如果是1-5表示周一到周五  是工作日
        DayOfWeek week = dateTime.getDayOfWeek();
        if(week==DayOfWeek.SATURDAY||week==DayOfWeek.SUNDAY){
            return false;
        }
        return true;

    }

    public static void main(String[] args) {
        initHoliday();
        initExtraWorkDay();
        Boolean workingDay = isWorkingDay(System.currentTimeMillis());
        if(workingDay){
            System.out.println("工作日，加油，打工人");
        }else {
            System.out.println("开开心心过节，高高兴兴干饭！！！");
        }
    }

    /**
     *  初始化节假日
     */
    public static void initHoliday(){
        holiday.add("2021-01-01");
        holiday.add("2021-01-02");
        holiday.add("2021-01-03");
        holiday.add("2021-02-11");
        holiday.add("2021-02-12");
        holiday.add("2021-02-13");
        holiday.add("2021-02-14");
        holiday.add("2021-02-15");
        holiday.add("2021-02-16");
        holiday.add("2021-02-17");
        holiday.add("2021-04-03");
        holiday.add("2021-04-04");
        holiday.add("2021-04-05");
        holiday.add("2021-05-01");
        holiday.add("2021-05-02");
        holiday.add("2021-05-03");
        holiday.add("2021-05-04");
        holiday.add("2021-05-05");
        holiday.add("2021-06-12");
        holiday.add("2021-06-13");
        holiday.add("2021-06-14");
        holiday.add("2021-09-19");
        holiday.add("2021-09-20");
        holiday.add("2021-09-21");
        holiday.add("2021-10-01");
        holiday.add("2021-10-02");
        holiday.add("2021-10-03");
        holiday.add("2021-10-04");
        holiday.add("2021-10-05");
        holiday.add("2021-10-06");
        holiday.add("2021-10-07");
    }
    /**
     *  初始化额外加班日
     */
    public static void initExtraWorkDay(){
        extraWorkDay.add("2021-02-07");
        extraWorkDay.add("2021-02-20");
        extraWorkDay.add("2021-04-25");
        extraWorkDay.add("2021-05-08");
        extraWorkDay.add("2021-09-18");
        extraWorkDay.add("2021-09-26");
        extraWorkDay.add("2021-10-09");
    }
}
