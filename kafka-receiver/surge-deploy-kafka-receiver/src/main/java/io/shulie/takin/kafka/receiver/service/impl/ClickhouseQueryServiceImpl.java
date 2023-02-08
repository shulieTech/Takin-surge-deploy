package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import io.shulie.takin.kafka.receiver.dto.clickhouse.ClickhouseQueryRequest;
import io.shulie.takin.kafka.receiver.service.ClickhouseQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sunsy
 * @date 2022/2/28
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Service("clickhouseQueryServiceImpl")
@Slf4j
public class ClickhouseQueryServiceImpl implements ClickhouseQueryService {
    private static String BLANK = " ";

    @Resource
    private ClickHouseSupport clickHouseSupport;

    @Override
    public List<Map<String, Object>> queryObjectByConditions(ClickhouseQueryRequest request) {
        List<Map<String, Object>> resultList = Lists.newArrayList();
        String querySql = buildQuerySql(request);
        if (StringUtils.isNotBlank(querySql)) {
            log.info("clickHouse query sql:{}", querySql);
            try {
                resultList = this.queryForList(querySql);
            } catch (Exception e) {
                log.error("query clickHouse catch exception:{},{}", e, e.getStackTrace());
                //查询sql，出现异常，返回空值
                return new ArrayList<>();
            }

        } else {
            log.info("拼接出来的sql为空，入参为:{}", JSONObject.toJSONString(request));
        }
        return resultList;
    }

    @Override
    public <T> List<T> queryObjectByConditions(ClickhouseQueryRequest request, Class<T> clazz) {
        String querySql = buildQuerySql(request);
        if (StringUtils.isNotBlank(querySql)) {
            log.info("clickHouse query sql:{}", querySql);
            try {
                List<T> tList = this.queryForList(querySql, clazz);
                if (CollectionUtils.isEmpty(tList)){
                    log.info("查询出来内容为空，sql为:{}", querySql);
                }
                return tList;
            } catch (Exception e) {
                log.error("query clickHouse catch exception:{},{}", e, e.getStackTrace());
                return null;
            }
        } else {
            log.info("拼接出来的sql为空，入参为:{}", JSONObject.toJSONString(request));
        }
        return null;
    }


    private String buildQuerySql(ClickhouseQueryRequest request) {
        StringBuilder sbuilder = new StringBuilder();

        sbuilder.append("select").append(BLANK);
        if (MapUtils.isNotEmpty(request.getAggregateStrategy())) {
            //aggregate fields
            sbuilder.append(parseAliasFields(request.getAggregateStrategy())).append(BLANK);
        }
        if (MapUtils.isNotEmpty(request.getFieldAndAlias())) {
            //non-aggregate fields
            sbuilder.append(parseAliasFields(request.getFieldAndAlias())).append(BLANK);
        }
        if (MapUtils.isEmpty(request.getAggregateStrategy()) && MapUtils.isEmpty(request.getFieldAndAlias())) {
            sbuilder.append("*").append(BLANK);
        }
        sbuilder.append("from").append(BLANK);

        if (!request.getMeasurement().endsWith("_all")) {
            sbuilder.append(request.getMeasurement()).append("_all").append(BLANK);
        } else {
            sbuilder.append(request.getMeasurement()).append(BLANK);
        }
        sbuilder.append("where 1=1").append(BLANK);
        if (request.getStartTime() > 0 && request.isStartTimeEqual()) {
            sbuilder.append("and time >=").append(BLANK);
            sbuilder.append(request.getStartTime()).append(BLANK);
        }
        if (request.getStartTime() > 0 && !request.isStartTimeEqual()) {
            sbuilder.append("and time >").append(BLANK);
            sbuilder.append(request.getStartTime()).append(BLANK);
        }
        if (request.getEndTime() > 0 && request.isEndTimeEqual()) {
            sbuilder.append("and time <=").append(BLANK);
            sbuilder.append(request.getEndTime()).append(BLANK);
        }
        if (request.getEndTime() > 0 && !request.isEndTimeEqual()) {
            sbuilder.append("and time <").append(BLANK);
            sbuilder.append(request.getEndTime()).append(BLANK);
        }
        if (CollectionUtils.isNotEmpty(request.getNotNullWhere())) {
            sbuilder.append("and").append(BLANK);
            sbuilder.append(parseNotNullWhere(request.getNotNullWhere())).append(BLANK);
        }
        if (MapUtils.isNotEmpty(request.getWhereFilter())) {
            sbuilder.append("and").append(BLANK);
            sbuilder.append(parseWhereFilter(request.getWhereFilter())).append(BLANK);
        }
        if (CollectionUtils.isNotEmpty(request.getGroupByTags())) {
            sbuilder.append(parseGroupBy(request.getGroupByTags())).append(BLANK);
        }
        if (request.getOrderByStrategy() != null && request.getOrderByStrategy() == 0) {
            sbuilder.append("order by time asc").append(BLANK);
        } else if (request.getOrderByStrategy() != null && request.getOrderByStrategy() == 1) {
            sbuilder.append("order by time desc").append(BLANK);
        }
        if (request.getLimitRows() > 0) {
            sbuilder.append("limit").append(BLANK).append(request.getLimitRows()).append(BLANK);
        }
        if (request.getLimitRows() > 0) {
            sbuilder.append("offset").append(BLANK).append(request.getOffset()).append(BLANK);
        }
        return sbuilder.toString();
    }

    private String parseNotNullWhere(List<String> notNullWhere) {
        List<String> wheres = new ArrayList<>();
        notNullWhere.forEach(where -> {
            wheres.add(where + "is not null");
        });
        return StringUtils.join(wheres, " and ");
    }

    private String parseGroupBy(List<String> groupFields) {
        return "group by " + StringUtils.join(groupFields, ",");
    }

    private String parseWhereFilter(Map<String, Object> tagMap) {
        List<String> inFilterList = new ArrayList<>();
        List<String> orFilterList = new ArrayList<>();
        tagMap.forEach((k, v) -> {
            if (v instanceof List) {
                if (((List<?>) v).size() <= 1) {
                    inFilterList.add(k + "='" + ((List<?>) v).get(0) + "'");
                } else {
                    //rpc服务的method含有形参,亦是用逗号分割,暂时过滤其他字段的or查询,只支持edgeId
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(");

                    for (Object single : (ArrayList) v) {
                        stringBuilder.append(k + "='" + single + "'").append(" or ");
                    }
                    stringBuilder.delete(stringBuilder.lastIndexOf(" or "), stringBuilder.length());
                    stringBuilder.append(")");
                    orFilterList.add(stringBuilder.toString());
                }
            } else {
                inFilterList.add(k + "='" + v + "'");
            }
        });
        if (orFilterList.isEmpty()) {
            return StringUtils.join(inFilterList, " and ");
        }
        return StringUtils.join(inFilterList, " and ") + " and " + StringUtils.join(orFilterList, " and ");
    }


    public static String parseAliasFields(Map<String, String> fieldsMap) {
        List<String> aliasList = new ArrayList<>();
        fieldsMap.forEach((k, v) -> {
            if (StringUtils.isBlank(v)) {
                aliasList.add(k + " as " + k);
            } else {
                aliasList.add(k + " as " + v);
            }
        });
        if (CollectionUtils.isEmpty(aliasList)) {
            return "*";
        }
        return StringUtils.join(aliasList, ",");
    }

    private List<Map<String, Object>> queryForList(String sql) {
        return clickHouseSupport.queryForList(sql);
    }

    private <T> List<T> queryForList(String sql, Class<T> clazz) {
        List<Map<String, Object>> resultList = queryForList(sql);
        if (resultList.size() == 0) {
            return new ArrayList<>();
        }
        return resultList.stream().map(result -> JSONObject.parseObject(JSON.toJSON(result).toString(), clazz)).collect(Collectors.toList());
    }

}
