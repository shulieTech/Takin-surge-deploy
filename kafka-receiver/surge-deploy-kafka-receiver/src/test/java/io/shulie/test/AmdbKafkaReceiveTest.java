package io.shulie.test;

import io.shulie.takin.kafka.receiver.Application;
import io.shulie.takin.kafka.receiver.service.IAmdbAppInstanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(
        SpringJUnit4ClassRunner.class
)
@ContextConfiguration(classes = Application.class)
public class AmdbKafkaReceiveTest {

    @Resource
    private IAmdbAppInstanceService iAmdbAppInstanceService;


    @Test
    public void test_dealPradarClientMessage(){
        //传入空参，不报错，不处理
        Map<String,Object> map = new HashMap<>();
        iAmdbAppInstanceService.dealPradarClientMessage(map);
    }
}
