package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 集群nacos配合
 * </p>
 *
 * @author zhaoyong
 * @since 2023-03-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_cluster_nacos_configuration")
@ApiModel(value = "ClusterNacosConfiguration对象", description = "集群nacos配合")
public class ClusterNacosConfiguration implements Serializable {


    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "集群名称")
    @TableField("cluster_name")
    private String clusterName;

    @ApiModelProperty(value = "nacos服务地址")
    @TableField("nacos_server_addr")
    private String nacosServerAddr;

    @ApiModelProperty(value = "nacos的命名空间")
    @TableField("nacos_namespace")
    private String nacosNamespace;

    @ApiModelProperty(value = "nacos用户名")
    @TableField("nacos_username")
    private String nacosUsername;

    @ApiModelProperty(value = "nacos密码")
    @TableField("nacos_password")
    private String nacosPassword;

    @ApiModelProperty(value = "创建时间")
    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    @ApiModelProperty(value = "最近更新时间")
    @TableField("gmt_update")
    private LocalDateTime gmtUpdate;


}
