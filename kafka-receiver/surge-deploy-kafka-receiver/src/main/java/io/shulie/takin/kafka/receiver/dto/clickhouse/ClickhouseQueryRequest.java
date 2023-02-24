package io.shulie.takin.kafka.receiver.dto.clickhouse;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/27
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@ApiModel("clickhouseQueryService")
@Data
public class ClickhouseQueryRequest {

    @ApiModelProperty("表名")
    private String measurement;
    @ApiModelProperty("开始时间")
    private long startTime;
    @ApiModelProperty("开始时间包含等于")
    private boolean startTimeEqual;
    @ApiModelProperty("结束时间")
    private long endTime;
    @ApiModelProperty("结束时间包含等于")
    private boolean endTimeEqual;
    @ApiModelProperty("查询字段及其别名")
    private Map<String, String> fieldAndAlias;
    @ApiModelProperty("where过滤条件")
    private Map<String, Object> whereFilter;
    /**
     * 不能为空字段
     */
    private List<String> notNullWhere;
    @ApiModelProperty("查询条数")
    private long limitRows;
    @ApiModelProperty("分页开始offset")
    private long offset;
    @ApiModelProperty("排序策略,0:升序;1:降序")
    private Integer orderByStrategy;
    @ApiModelProperty("分组tag")
    private List<String> groupByTags;
    @ApiModelProperty("聚合字段")
    private Map<String, String> aggregateStrategy;
}

