package io.shulie.surge.data.deploy.pradar;

/**
 * kafka 任务开关
 *
 * @author vincent
 * @date 2022/11/18 14:50
 **/
public class PradarKafkaSwitcher {

    public static final boolean SUPPLIER_SWITCHER = Boolean.parseBoolean(System.getProperty("switcher.supplier", "false"));
    public static final boolean LINK_TASK_SWITCHER = Boolean.parseBoolean(System.getProperty("switcher.link.task", "false"));
    public static final boolean AGGREGATION_SWITCHER = Boolean.parseBoolean(System.getProperty("switcher.aggregation", "false"));
}
