package io.shulie.surge.data.deploy.pradar.model;

import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/25
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public class EngineDateModel {
    private Long time;
    private Long eventTime;
    private String database = "engine";
    private String measurement;
    private List<Map<String, String>> tag;
    private List<Map<String, Object>> field;


    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public List<Map<String, String>> getTag() {
        return tag;
    }

    public void setTag(List<Map<String, String>> tag) {
        this.tag = tag;
    }

    public List<Map<String, Object>> getField() {
        return field;
    }

    public void setField(List<Map<String, Object>> field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "EngineDateModel{" +
                "time=" + time +
                ", measurement='" + measurement + '\'' +
                ", tag=" + tag +
                ", field=" + field +
                '}';
    }
}
