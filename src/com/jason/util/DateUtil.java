package com.jason.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    /**
     * 获取当前日期
     *
     * @return yyyy-MM-dd
     */
    public static String currentDate() {
        return dateFormat.format(now());
    }


    /**
     * 格式化日期时间
     *
     * @param date java.util.Date及其子类
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatDatetime(Date date) {
        return datetimeFormat.format(date);
    }

    /**
     * 格式化日期
     *
     * @param date java.util.Date及其子类
     * @return yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    /**
     * 以yyyy-MM-dd 00:00:00格式返回当天零点
     *
     * @return yyyy-MM-dd 00:00:00
     */
    public static String getTodayStartTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return formatDatetime(cal.getTime());
    }

    public static long getTodayStartTimeLong() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getSpecifiedAfterDayStartTime(String specifiedDate) {
        Date date = null;
        try {
            date = dateFormat.parse(specifiedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        int day = cl.get(Calendar.DATE);
        cl.set(Calendar.DATE, day + 1);
        return cl.getTimeInMillis();
    }

    public static long getSpecifiedDayStartTime(String specifiedDate) {
        long time = 0;
        try {
            time = dateFormat.parse(specifiedDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static boolean isBefore(Date src, Date dest) {
        return src.before(dest);
    }

    public static Date now() {
        return new Date();
    }

    public static void main(String[] args) {
        System.out.println(getTodayStartTime());
    }
}
