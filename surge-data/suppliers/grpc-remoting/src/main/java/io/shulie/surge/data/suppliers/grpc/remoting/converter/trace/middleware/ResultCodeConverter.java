package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.MiddlewareType;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2022/8/19 17:03
 */
public class ResultCodeConverter implements MiddlewareConverter {
    /**
     * 返回成功
     */
    static public final String INVOKE_RESULT_SUCCESS = "00";

    /**
     * 返回失败，一般是业务失败
     */
    static public final String INVOKE_RESULT_FAILED = "01";

    /**
     * 返回业务错误
     */
    static public final String INVOKE_RESULT_BIZ_ERR = "02";

    /**
     * 返回超时错误
     */
    static public final String INVOKE_RESULT_TIMEOUT = "03";

    /**
     * 未知
     */
    static public final String INVOKE_RESULT_UNKNOWN = "04";

    /**
     * 断言失败
     */
    static public final String INVOKE_ASSERT_RESULT_FAILED = "05";

    @Override
    public void convert(Span span, TraceProtoBean traceProtoBean) {
        String resultCode = traceProtoBean.getResultCode();
        if (traceProtoBean.getMiddlewareType() == MiddlewareType.TYPE_WEB_SERVER.getType()
                || traceProtoBean.getMiddlewareType() == MiddlewareType.TYPE_HTTP_CLIENT.getType()) {
            try {
                if(INVOKE_RESULT_FAILED.equals(resultCode) || INVOKE_RESULT_BIZ_ERR.equals(resultCode) ||
                    INVOKE_RESULT_TIMEOUT.equals(resultCode) || INVOKE_RESULT_UNKNOWN.equals(resultCode) || INVOKE_ASSERT_RESULT_FAILED.equals(resultCode)
                ) {
                    // 直接返回 01 则直接错误 原因：服务端未启动场景
                    traceProtoBean.setSuccess(TraceProtoBean.FAIL);
                }else {
                    int code = Integer.parseInt(resultCode);
                    traceProtoBean.setSuccess(code < 400 ? TraceProtoBean.SUCCESS : TraceProtoBean.FAIL);
                }
            } catch (NumberFormatException e) {
                traceProtoBean.setSuccess(TraceProtoBean.FAIL);
            }
        } else {
            traceProtoBean.setSuccess((StringUtils.equals(resultCode, INVOKE_RESULT_SUCCESS)) ? TraceProtoBean.SUCCESS : TraceProtoBean.FAIL);
        }

    }
}
