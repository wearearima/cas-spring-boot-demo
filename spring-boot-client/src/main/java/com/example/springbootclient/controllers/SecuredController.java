package com.example.springbootclient.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SecuredController {

    private Logger logger = LoggerFactory.getLogger(SecuredController.class);

    @GetMapping("/secured")
    public ModelAndView securedIndex(ModelMap modelMap) {

        logger.info("/secured called");

        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();

        if(auth.getPrincipal() instanceof UserDetails)
            modelMap.put("username", ((UserDetails) auth.getPrincipal()).getUsername());

        logger.info("SecuredController", auth.getPrincipal().toString());

        return new ModelAndView("secure/index", modelMap);
    }
}
