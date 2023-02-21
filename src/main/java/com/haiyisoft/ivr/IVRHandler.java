package com.haiyisoft.ivr;

import com.alibaba.fastjson.JSONObject;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.util.IdGenerator;
import com.haiyisoft.util.NGDUtil;
import com.haiyisoft.util.XCCUtil;
import com.sun.org.apache.bcel.internal.generic.RET;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By Chryl on 2023-02-08.
 */
@Slf4j
@Component
public class IVRHandler {

    /**
     * 一定要交给spring管理,哪怕是此方法被调用
     */
    @Async
    public void ivrDomain(Connection nc, JSONObject params) {
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                System.out.println(Thread.currentThread().getName() + "===" + IdGenerator.simpleUUID());
            }
        }
    }


    @Async
    public void HandlerChannelEvent(Connection nc, ChannelEvent event) {
        switch (event.getState()) {
            case XCCConstants.Channel_START:
                //开始接管
                XCCUtil.accept(nc, event);
//                XCCUtil.playTTS(nc, event, XCCConstants.WELCOME_TEXT);

                //播报欢迎语收音
                String speechMsg = XCCUtil.detectSpeechPlayTTSNoDTMF(nc, event, XCCConstants.WELCOME_TEXT);
                //调用多轮
                String res = NGDUtil.doGxZsk(speechMsg);
                //处理指令和内容
                Map<String, String> resMap = NGDUtil.convertResText(res);
                //获取多轮指令和内容
                String retKey = resMap.get("retKey");
                String retValue = resMap.get("retValue");
                while (true) {
                    if (XCCConstants.YYSR.equals(retKey)) {
                        //调用播报收音
                        speechMsg = XCCUtil.detectSpeechPlayTTSNoDTMF(nc, event, retValue);
                    } else if (XCCConstants.AJSR.equals(retKey)) {
                        //调用xcc收集按键方法，多位按键
                        speechMsg = XCCUtil.playAndReadDTMF(nc, event, retValue, 18);
                    } else if (XCCConstants.YWAJ.equals(retKey)) {
                        //调用xcc收集按键方法，一位按键
                        speechMsg = XCCUtil.playAndReadDTMF(nc, event, retValue, 1);
                    }
                    res = NGDUtil.doGxZsk(speechMsg);
                    resMap = NGDUtil.convertResText(res);
                    retKey = resMap.get("retKey");
                    retValue = resMap.get("retValue");

                }


//                xcc.SetVar(nc, event);
                //播放一段音频
//                xcc.Play(nc, event);
                //获取当前通道状态
//                xcc.GetState(nc, event);
                //执行原生APP
//                xcc.NativeApp(nc, event);
                //语音进入IVR
//                IvrHandler ivr = new IvrHandler();
//                ivr.Ivr(nc, event);

            case "CALLING":
            case "RINGING":
            case "BRIDGE":
            case "READY":
//                xcc.Bridge(nc, event);
            case "MEDIA":
        }

        XCCUtil.hangup(nc, event);

    }

}
