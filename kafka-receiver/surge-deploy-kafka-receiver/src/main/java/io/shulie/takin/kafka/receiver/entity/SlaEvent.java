package io.shulie.takin.kafka.receiver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * sla触发记录
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-13
 */
@TableName("t_sla_event")
@ApiModel(value="SlaEvent对象", description="sla触发记录")
public class SlaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务实例")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "sla主键")
    private Long slaId;

    @ApiModelProperty(value = "任务主键")
    private Long pressureId;

    @ApiModelProperty(value = "任务实例主键")
    private Long pressureExampleId;

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

    @ApiModelProperty(value = "比较的值(实际变化的值)")
    private Double number;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSlaId() {
        return slaId;
    }

    public void setSlaId(Long slaId) {
        this.slaId = slaId;
    }

    public Long getPressureId() {
        return pressureId;
    }

    public void setPressureId(Long pressureId) {
        this.pressureId = pressureId;
    }

    public Long getPressureExampleId() {
        return pressureExampleId;
    }

    public void setPressureExampleId(Long pressureExampleId) {
        this.pressureExampleId = pressureExampleId;
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

    public Double getNumber() {
        return number;
    }

    public void setNumber(Double number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "SlaEvent{" +
        "id=" + id +
        ", slaId=" + slaId +
        ", pressureId=" + pressureId +
        ", pressureExampleId=" + pressureExampleId +
        ", ref=" + ref +
        ", attach=" + attach +
        ", formulaTarget=" + formulaTarget +
        ", formulaSymbol=" + formulaSymbol +
        ", formulaNumber=" + formulaNumber +
        ", number=" + number +
        "}";
    }
}
