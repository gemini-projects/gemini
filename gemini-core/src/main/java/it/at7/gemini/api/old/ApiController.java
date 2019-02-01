package it.at7.gemini.api.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {


        throw new Exception("AAA");
    /*
        MappingJackson2JsonView res = new MappingJackson2JsonView();
        ModelAndView mv = new ModelAndView(res);
        mv.addObject("test", "testValue");
        return mv;*/
    }
}
