package com.daqiao.imdc;

import com.daqiao.imdc.config.ImdcConfig;
import com.daqiao.imdc.config.SystemEnv;
import com.daqiao.imdc.service.FaceService;
import com.daqiao.imdc.view.HttpFaceInfo;
import com.daqiao.imdc.view.HttpFaceResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

@Slf4j
@Component
public class MainFrame extends JFrame {

    // 序列化
    private static final long serialVersionUID = 1L;
    // 面板
    private JPanel jPanel;
    // 宽度
    private static final int WIDTH = 400;
    // 高度
    private static final int HEIGHT = 300;

    @Autowired
    private SystemEnv systemEnv;

    @Autowired
    private ImdcConfig imdcConfig;

    @Autowired
    private FaceService faceService;

    private JTextField textField;

    @Setter
    private String title = "人脸识别控制面板";

    public MainFrame() {
        if (StringUtils.hasText(title)) {
            setTitle(title);
        }
        setSize(WIDTH, HEIGHT);

        //得到窗口的容器
        Container conn = getContentPane();
        //
        jPanel = new JPanel();
        //创建一个标签 并设置初始内容
        JButton jButton1 = new JButton("start");
        jButton1.setSize(20, 10);
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startButtonOnClick();
            }
        });

        JButton jButton2 = new JButton("stop");
        jButton2.setSize(20, 10);
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopFace();
            }
        });

        JButton jButton3 = new JButton("checkHealth");
        jButton3.setSize(20, 10);
        jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkHealthButtonOnClick();
            }
        });


        textField = new JTextField(10);

        jPanel.add(textField);
        jPanel.add(jButton1);
        jPanel.add(jButton2);
        jPanel.add(jButton3);

        conn.add(jPanel);

        //设置窗口的属性 窗口位置以及窗口的大小
        setBounds(200, 200, 480, 240);
        //设置窗口可见
        setVisible(true);
        //设置关闭方式 如果不设置的话 似乎关闭窗口之后不会退出程序
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void checkHealthButtonOnClick() {
        HttpFaceResult result = checkHealth();
        JOptionPane.showMessageDialog(null, String.format("%s", result.getResult()));
    }

    public void startButtonOnClick() {
        HttpFaceInfo httpFaceInfo = new HttpFaceInfo();
        if (!StringUtils.hasText(textField.getText())) {
            throw new RuntimeException("args is empty.");
        } else {
            httpFaceInfo.setUserId(textField.getText());
        }
        log.info("{}", httpFaceInfo.getUserId());
        startFace(httpFaceInfo);

        new Thread(() -> {
            HttpFaceResult result = new HttpFaceResult();
            int counter = 0;
            boolean isTimeout = false;
            while (!systemEnv.isFaceStop() && !systemEnv.isFaceSuccessful()) {
                if (counter > imdcConfig.getTimeout()) {
                    isTimeout = true;
                    break;
                }
                counter++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 超时
            if (isTimeout) {
                log.error("人脸识别超时");
                result.setResult("Timeout");
            } else if (systemEnv.isFaceStop()) {
                log.error("人脸识别被强制终止");
                result.setResult("FaceStop");
            } else if (systemEnv.isFaceSuccessful()) {
                log.info("识别成功");
                result.setResult("Successful");
                // 图片索引、分数
                result.setIndex(systemEnv.getHttpFaceResult().getIndex());
                result.setScore(systemEnv.getHttpFaceResult().getScore());
            } else {
                log.info("识别失败");
                result.setResult("SystemError");
            }
            if (Objects.equals(result.getResult(), "Successful")) {
                // 停掉所有的人脸识别线程
                faceService.stop();
                JOptionPane.showMessageDialog(null, String.format("%s, %d, %f", result.getResult(), result.getIndex(), result.getScore()));
            } else {
                // 停掉所有的人脸识别线程
                faceService.stop();
                JOptionPane.showMessageDialog(null, String.format("%s", result.getResult()));
            }
            systemEnv.setFaceStop(true);
            try {
                this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public HttpFaceResult startFace(HttpFaceInfo httpFaceInfo) {
        faceService.start(httpFaceInfo.getUserId());
        return new HttpFaceResult();
    }

    public boolean stopFace() {
        faceService.stop();
        return true;
    }

    public HttpFaceResult checkHealth() {
        return new HttpFaceResult("Yes");
    }
}
