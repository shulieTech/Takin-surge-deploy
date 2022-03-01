package io.shulie.surge.data.deploy.pradar.model;

/**
 * @author Sunsy
 * @date 2022/2/25
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public class ResponseDataModel {
    private long time;
    private String responseCode;
    private String responseMsg;

    public ResponseDataModel(long time, String responseCode, String responseMsg) {
        this.time = time;
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    @Override
    public String toString() {
        return "ResponseDataModel{" +
                "time=" + time +
                ", responseCode='" + responseCode + '\'' +
                ", responseMsg='" + responseMsg + '\'' +
                '}';
    }
}
