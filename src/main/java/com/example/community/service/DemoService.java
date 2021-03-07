package com.example.community.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@Scope("prototype")
public class DemoService {
    public DemoService() {
        System.out.println("Initial");
    }
    public DemoService(int i) {
        System.out.println("Initial_1");
    }

        @PostConstruct
        public void Post(){
            System.out.println("Instance");
        }
        @PreDestroy
        public void destory(){
            System.out.println("destory");
        }
}
