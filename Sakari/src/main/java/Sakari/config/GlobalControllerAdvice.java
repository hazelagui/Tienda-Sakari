package Sakari.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("activePage")
    public String activePage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        if (uri.equals("/") || uri.equals("/inicio") || uri.equals("/home")) {
            return "inicio";
        } else if (uri.startsWith("/productos")) {
            return "productos";
        } else if (uri.equals("/personalizaciones")) {
            return "personalizaciones";
        } else if (uri.equals("/quienes-somos")) {
            return "quienes-somos";
        } else if (uri.equals("/contacto")) {
            return "contacto";
        } else if (uri.startsWith("/admin")) {
            return "admin";
        } else if (uri.startsWith("/carrito")) {
            return "carrito";
        } else if (uri.startsWith("/perfil")) {
            return "perfil";
        }
        
        return "";
    }
}
