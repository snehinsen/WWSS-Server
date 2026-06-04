package ca.tlcp.hpsocialsserver.api.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ForwardController {

    @RequestMapping(value = ["/login", "/register", "/setup", "/forgot-password"])
    fun forwardLoginPage(): String {
        return "forward:/index.html"
    }

    @RequestMapping(value = ["/"])
    fun forwardRootURL(): String {
        return "forward:/index.html"
    }

    @GetMapping(value = ["/app/**", "/app/*/**", "/app/*/*/**", "/app/*/*/*/**"])
    fun forwardUserUI(): String {

        return "forward:/index.html"
    }

    @GetMapping(value = ["/admin/**", "/admin/*/**", "/admin/*/*/**", "/admin/*/*/*/**"])
    fun forwardAdminUI(): String {

        return "forward:/admin.html"
    }

}
