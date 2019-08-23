package it.at7.gemini.gui.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardController {

    @RequestMapping(value = "/**/{[path:[^\\.]*}")
    public String redirect() {
        // TODO check that forward work in production
        return "forward:/";
    }
}