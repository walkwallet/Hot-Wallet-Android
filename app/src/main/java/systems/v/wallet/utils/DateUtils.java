package systems.v.wallet.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtils {
    public static String format(long time, String format) {
        time = getTimeMillis(time);
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date(time));
    }

    public static long getTimeMillis(long time) {
        if (Math.abs(time) < 9999999999L) {
            return time * 1000;
        }
        return time;
    }

    public static String formatyMd(long time) {
        return format(time, "yyyy-MM-dd");
    }

    public static String getMonth(long time) {
        String yMd = formatyMd(time);
        String year = yMd.substring(0, 4);
        String month = yMd.substring(5, 7);
        String day = yMd.substring(8, 10);
        if (month.startsWith("0")) {
            month = month.substring(1, 2);
        }
        try {
            int lan = SPUtils.getInt(Constants.LANGUAGE);
            String resultMon = "";
            switch (lan) {
                case Constants.LAN_EN_US:
                    resultMon = getMonthEn(Integer.parseInt(month));
                    break;
                case Constants.LAN_ZH_CN:
                    resultMon = getMonthZh(Integer.parseInt(month));
                    break;
                default:
                    resultMon = getMonthEn(Integer.parseInt(month));
                    break;
            }
            return resultMon;/* + " " + day + ", " + year;*/
            //return
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMonthDayYear(long time) {
        String yMd = formatyMd(time);
        String year = yMd.substring(0, 4);
        String month = yMd.substring(5, 7);
        String day = yMd.substring(8, 10);
        if (month.startsWith("0")) {
            month = month.substring(1, 2);
        }
        try {
            String resultMon = getMonthEn(Integer.parseInt(month));
            return resultMon + " " + day + ", " + year;
            //return
        } catch (Exception e) {
            return "";
        }
    }

    public static long StringDateToLong(String date) {
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Long time = df.parse(date).getTime();
            return time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String formatY(long time) {
        String s = formatyMd(time);
        return s.substring(0, 4);
    }

    public static String formatyM(long time) {
        String s = formatyMd(time);
        return s.substring(0, 7);
    }

    public static String formatD(long time) {
        String s = formatyMd(time);
        return s.substring(8, 10);
    }

    public static long getFormat(long timeStamp) {
        String time = String.valueOf(timeStamp);
        int times = 0;
        if (time.length() > 13) {
            time = time.substring(0, 13);
            timeStamp = Long.parseLong(time);
        } else if (time.length() < 13) {
            int x = 13 - time.length();
            times = (int) Math.pow(10, x);
            timeStamp = timeStamp * times;
        }
        return timeStamp;
    }

    public static String formatHm(long time) {
        return format(time, "HH:mm");
    }

    public static String getShowTime(long timeStamp) {
        long now = System.currentTimeMillis();
        String nowYear = formatY(now);
        String nowDay = formatD(now);
        String nowMonth = formatyM(now);

        String year = formatY(timeStamp);
        String day = formatD(timeStamp);
        String month = formatyM(timeStamp);

        if (month.equals(nowMonth)) {//same moth
            if (day.equals(nowDay)) {//same day
                return formatHm(timeStamp);
            } else {//not same day
                String Md = format(timeStamp, "MM.dd");
                String Hm = formatHm(timeStamp);
                return Md + "," + Hm;
            }
        } else if (year.equals(nowYear)) {//same year
            String Md = format(timeStamp, "MM.dd");
            String Hm = formatHm(timeStamp);
            return Md + "," + Hm;
        } else {//not same year
            String y = format(timeStamp, "yyyy");
            String Md = format(timeStamp, "MM.dd");
            String Hm = formatHm(timeStamp);
            return Md + "." + y + "," + Hm;

        }
    }

    private static String getMonthEn(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "";
        }
    }

    private static String getMonthZh(int month) {
        switch (month) {
            case 1:
                return "一月";
            case 2:
                return "二月";
            case 3:
                return "三月";
            case 4:
                return "四月";
            case 5:
                return "五月";
            case 6:
                return "六月";
            case 7:
                return "七月";
            case 8:
                return "八月";
            case 9:
                return "九月";
            case 10:
                return "十月";
            case 11:
                return "十一月";
            case 12:
                return "十二月";
            default:
                return "";
        }
    }
}
