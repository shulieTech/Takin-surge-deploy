package io.shulie.takin.kafka.receiver.constant.web;

/**
 * 常量
 *
 * @author <a href="tangyuhan@shulie.io">yuhan.tang</a>
 * @date 2020-04-20 15:55
 */
public class CollectorConstants {

    /**
     * 窗口大小
     */
    public static int[] timeWindow = new int[] {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
    /**
     * 单位：秒
     */
    public static final int OVERDUE_SECOND = 10;
    /**
     * Metrics 统计时间间隔
     */
    public static final int SEND_TIME = 5;
    public static final int SECOND_60 = 60;

}
