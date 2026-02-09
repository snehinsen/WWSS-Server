package ca.tlcp.hpsocialsserver

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ForwardController {

    @RequestMapping(value = ["/login"])
    fun forwardLoginPage(): String {
        return "redirect:/app/login"
    }

    @RequestMapping(value = ["/"])
    fun forwardRootURL(): String {
        return "redirect:/app/"
    }

    @GetMapping(value = ["/app/*", "/app/*/**", "/app/*/*/**", "/app/*/*/*/**"])
    fun forward(): String {

        return "forward:/index.html"
    }

}
