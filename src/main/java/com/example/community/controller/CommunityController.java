package com.example.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/community")
@Controller
public class CommunityController {

    @RequestMapping("/hello")
    @ResponseBody
    public String tesrt1(){
        return "hello world\n";
    }

    @RequestMapping(path = "/student",method = RequestMethod.POST) //声明是GET请求
    @ResponseBody
    public  String getStudent(String name, int age){
        System.out.println(name+""+age);
        return "student";
    }

    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav=new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",30);
        mav.setViewName("/demo/view"); //将
        return mav;
    }
}
