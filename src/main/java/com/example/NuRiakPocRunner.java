package com.example;

import com.example.config.DemoConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class NuRiakPocRunner {

    @Autowired
    private static DemoConfig demoConfig;

    public static void main (String[] args) throws Exception{
        DemoApplication demoApplication = demoConfig.getDemoApplication();
        demoApplication.run();
    }
}
