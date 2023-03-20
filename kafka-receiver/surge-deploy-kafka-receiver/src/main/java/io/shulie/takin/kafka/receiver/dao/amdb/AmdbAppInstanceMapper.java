package io.shulie.takin.kafka.receiver.dao.amdb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.shulie.takin.kafka.receiver.entity.AmdbAppInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-14
 */
@Mapper
public interface AmdbAppInstanceMapper extends BaseMapper<AmdbAppInstance> {

    @Update("update t_amdb_app_instance set flag=(flag^1) where (flag&1)=1 and gmt_modify < #{date}")
    int initOnlineStatus(@Param("date") Date date);
}
