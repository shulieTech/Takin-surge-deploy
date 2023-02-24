package io.shulie.takin.kafka.receiver.dto.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description agent命令请求
 * @Author ocean_wll
 * @Date 2021/11/11 2:29 下午
 */
@Data
@ApiModel("入参类-agent命令请求")
public class AgentHeartbeatRequest {

    @ApiModelProperty(value = "应用名", required = true)
    @NotBlank(message = "应用名不能为空")
    private String projectName;

    @ApiModelProperty(value = "agentId", required = true)
    @NotBlank(message = "agentId不能为空")
    private String agentId;

    @ApiModelProperty(value = "ip地址", required = true)
    @NotBlank(message = "ip地址不能为空")
    private String ipAddress;

    @ApiModelProperty(value = "进程号", required = true)
    @NotBlank(message = "进程号不能为空")
    private String progressId;

    @ApiModelProperty(value = "当前批次号", required = true)
    @NotBlank(message = "当前批次号不能为空")
    private String curUpgradeBatch;

    @ApiModelProperty(value = "agent状态", required = true)
    @NotBlank(message = "agent状态不能为空")
    private String agentStatus;

    @ApiModelProperty(value = "agent错误信息")
    private String agentErrorInfo;

    @ApiModelProperty(value = "simulator状态")
    private String simulatorStatus;

    @ApiModelProperty(value = "simulator错误信息")
    private String simulatorErrorInfo;

    @ApiModelProperty(value = "卸载状态", required = true)
    @NotNull(message = "卸载状态不能为空")
    private Integer uninstallStatus;

    @ApiModelProperty(value = "休眠状态", required = true)
    @NotNull(message = "休眠状态不能为空")
    private Integer dormantStatus;

    @ApiModelProperty(value = "agent版本", required = true)
    @NotBlank(message = "agent版本不能为空")
    private String agentVersion;

    @ApiModelProperty(value = "simulator版本")
    private String simulatorVersion;

    @ApiModelProperty(value = "agent依赖的版本信息")
    private String dependencyInfo;

    @ApiModelProperty(value = "agent命令结果")
    private List<AgentCommandReqBO> commandResult;

    @ApiModelProperty(value = "是否企业版，开源版就是个空的值，企业版会给个shulieEnterprise")
    private String flag;

}
