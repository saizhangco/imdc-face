package com.daqiao.imdc;

import com.daqiao.imdc.config.ImdcConfig;
import com.daqiao.imdc.config.SystemEnv;
import com.daqiao.imdc.service.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class Main implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false).run(args);
    }

    @Autowired
    private FaceService faceService;

    @Autowired
    private ImdcConfig imdcConfig;

    @Autowired
    private SystemEnv systemEnv;

    @Autowired
    private MainFrame mainFrame;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        faceService.init();
        mainFrame.setTitle("Face Control Panel");
    }
}
