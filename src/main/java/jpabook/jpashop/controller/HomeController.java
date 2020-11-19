package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.logging.Logger;

@Controller
@Slf4j
public class HomeController {

/*
    //이런식으로 logger뽑을 수 있는데, 롬복 어노테이션으로 대체 가능
    org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
*/

    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        return "home";
    }
}
