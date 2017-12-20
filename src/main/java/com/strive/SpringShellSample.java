package com.strive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.plugin.PromptProvider;

/**
 * @ClassName: SpringShellSample
 * @Description: 说明
 * @author: tanggang@winshare-edu.com
 * @date: 2017年12月13日 15:04
 */
@SpringBootApplication
public class SpringShellSample {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringShellSample.class, args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return new PromptProvider() {
            @Override
            public String getPrompt() {
                return "my-shell:>";
            }

            @Override
            public String getProviderName() {
                return "my prompt provider";
            }
        };
    }
}
