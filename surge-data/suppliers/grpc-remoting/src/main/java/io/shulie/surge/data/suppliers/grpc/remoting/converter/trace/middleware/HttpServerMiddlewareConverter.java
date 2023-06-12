package io.shulie.surge.data.suppliers.grpc.remoting.converter.trace.middleware;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.shulie.surge.data.suppliers.grpc.remoting.converter.ConvertUtils;
import io.shulie.surge.data.suppliers.grpc.remoting.trace.TraceProtoBean;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author vincent
 * @date 2022/07/02 15:42
 **/
public class HttpServerMiddlewareConverter implements MiddlewareConverter {

    private static final String HTTP_REFER = "http.referer";
    //TODO 参数改正
    private static final String HTTP_ROUTE = "http.route";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_SCHEMA = "http.scheme";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String HTTP_METHOD = "http.method";

    /**
     * 属性转换
     *
     * @param span
     * @param traceBean
     */
    @Override
    public void convert(Span span, TraceProtoBean traceBean) {
        /**
         * traceBean.setAttributes("");
         * traceBean.setMethod();
         * traceBean.setService();
         * traceBean.setResultCode();
         * traceBean.setComment();
         */
        List<KeyValue> keyValues = span.getAttributesList();
        Map<String, String> attributes = ConvertUtils.attributeToMap(keyValues);

        if (attributes.containsKey(HTTP_ROUTE)) {
            traceBean.setService(attributes.get(HTTP_ROUTE));
        }
        if (StringUtils.isBlank(traceBean.getService())) {
            String url = attributes.get(HTTP_URL);
            if (-1 != StringUtils.indexOf(url, "?")) {
                url = StringUtils.substring(url, 0, url.indexOf("?"));
            }
            traceBean.setService(url);
        }
        if (StringUtils.isBlank(traceBean.getService())) {
            throw new IllegalArgumentException(HTTP_URL + " " + HTTP_ROUTE + " is null" + span);
        }

        if (attributes.containsKey(HTTP_METHOD)) {
            traceBean.setMethod(attributes.get(HTTP_METHOD));
        } else {
            throw new IllegalArgumentException(HTTP_METHOD + " is null" + span);
        }

    }

}
