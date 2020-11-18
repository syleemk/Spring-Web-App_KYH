package jpabook.jpashop.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model){//뷰에 넘기는 데이터 = Model
        model.addAttribute("data", "hello!!!");
        return "hello";
    }
}
