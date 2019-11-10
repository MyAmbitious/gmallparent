package com.alex.gmall.logger.controller;

import com.alex.gmall.common.constant.GmallConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoggerController {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    // @RequestMapping(name = "/log",method = RequestMethod.POST)
    @PostMapping("log")
    public String dolog(@RequestParam("logString") String logString) {
        //1 补充时间戳
        final JSONObject jsonObject = JSON.parseObject(logString);
        jsonObject.put("ts", System.currentTimeMillis());

        final String logJson = jsonObject.toJSONString();
        // 2 写日志（用于离线采集）
        log.info(logJson);

        //3 发送数据到kafka
        //将字符串写在前面 可以防止空指针
        if ("startup".equals(jsonObject.getString("type"))) {
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_STARTUP, logJson);
        } else {
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_EVENT, logJson);
        }

        return "success";
    }

}
