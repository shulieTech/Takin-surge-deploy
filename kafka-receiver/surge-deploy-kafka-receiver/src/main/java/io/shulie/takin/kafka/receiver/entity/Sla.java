package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Service Level Agreement(服务等级协议)
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_sla")
@ApiModel(value="Sla对象", description="Service Level Agreement(服务等级协议)")
public class Sla implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务实例")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务主键")
    private Long pressureId;

    @ApiModelProperty(value = "关键词")
    private String ref;

    @ApiModelProperty(value = "附加数据")
    private String attach;

    @ApiModelProperty(value = "算式目标(RT、TPS、SA、成功率)")
    private Integer formulaTarget;

    @ApiModelProperty(value = "算式符号(>=、>、=、<=、<)")
    private Integer formulaSymbol;

    @ApiModelProperty(value = "算式数值(用户输入)")
    private Double formulaNumber;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPressureId() {
        return pressureId;
    }

    public void setPressureId(Long pressureId) {
        this.pressureId = pressureId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public Integer getFormulaTarget() {
        return formulaTarget;
    }

    public void setFormulaTarget(Integer formulaTarget) {
        this.formulaTarget = formulaTarget;
    }

    public Integer getFormulaSymbol() {
        return formulaSymbol;
    }

    public void setFormulaSymbol(Integer formulaSymbol) {
        this.formulaSymbol = formulaSymbol;
    }

    public Double getFormulaNumber() {
        return formulaNumber;
    }

    public void setFormulaNumber(Double formulaNumber) {
        this.formulaNumber = formulaNumber;
    }

    @Override
    public String toString() {
        return "Sla{" +
        "id=" + id +
        ", pressureId=" + pressureId +
        ", ref=" + ref +
        ", attach=" + attach +
        ", formulaTarget=" + formulaTarget +
        ", formulaSymbol=" + formulaSymbol +
        ", formulaNumber=" + formulaNumber +
        "}";
    }
}
