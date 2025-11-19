package Sakari.controller;

import Sakari.service.CategoriaService;
import Sakari.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        model.addAttribute("productos", productoService.listarDisponibles());
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "publico/index";  // Cambio aquí
    }

    @GetMapping("/quienes-somos")
    public String quienesSomos() {
        return "publico/quienes-somos";  // Cambio aquí
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "publico/contacto";  // Cambio aquí
    }

    @GetMapping("/politicas-privacidad")
    public String politicasPrivacidad() {
        return "publico/politicas-privacidad";  // Cambio aquí
    }

    @GetMapping("/terminos-condiciones")
    public String terminosCondiciones() {
        return "publico/terminos-condiciones";  // Cambio aquí
    }
}