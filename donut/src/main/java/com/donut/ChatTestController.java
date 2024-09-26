package com.donut;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ChatTestController {
    @RequestMapping("/curriculum")
    public String curriculumTest(){
        return "chat.html";
    }
}
