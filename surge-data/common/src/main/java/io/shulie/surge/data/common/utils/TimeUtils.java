package io.shulie.surge.data.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Sunsy
 * @date 2022/3/4
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public class TimeUtils {

    /**
     * 5s时间窗口大小
     */
    public static int[] timeWindow_5 = new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
    /**
     * 5s时间窗口大小
     */
    public static int[] timeWindow_10 = new int[]{0, 10, 20, 30, 40, 50, 60};
    /**
     * 5s时间窗口大小
     */
    public static int[] timeWindow_30 = new int[]{0, 30, 60};

    public static final int SECOND_60 = 60;

    /**
     * 时间窗口格式化
     * 取值 5秒以内
     * >0 - <=5 取值 秒：5
     * >5 - <=10 取值 秒：10
     * >55 - <=60 取值 秒：0   分钟：+1
     *
     * @param timestamp 时间戳
     * @return -
     */
    public static Calendar getTimeWindow(long timestamp, int type) {
        int nowSecond = 0;
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timestamp);

        int second = instance.get(Calendar.SECOND);
        int millSecond = instance.get(Calendar.MILLISECOND);
        if (millSecond > 0) {
            second = second + 1;
        }

        int[] timeWindow;
        switch (type) {
            case 1:
                timeWindow = timeWindow_5;
                break;
            case 2:
                timeWindow = timeWindow_10;
                break;
            case 3:
                timeWindow = timeWindow_30;
                break;
            default:
                return instance;
        }

        for (int time : timeWindow) {
            if (second <= time) {
                nowSecond = time;
                break;
            }
        }
        instance.set(Calendar.MILLISECOND, 0);
        if (SECOND_60 == nowSecond) {
            instance.set(Calendar.SECOND, 0);
            instance.add(Calendar.MINUTE, 1);
        } else {
            instance.set(Calendar.SECOND, nowSecond);
        }
        return instance;
    }

}
