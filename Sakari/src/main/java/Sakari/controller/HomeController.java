package Sakari.controller;

import Sakari.service.ProductoService;
import Sakari.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping({"/", "/inicio", "/home"})
    public String inicio(Model model) {
        // Obtener productos destacados (primeros 8 productos activos)
        var productos = productoService.listarActivos();
        if (productos.size() > 8) {
            productos = productos.subList(0, 8);
        }
        model.addAttribute("productosDestacados", productos);
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "index";
    }

    @GetMapping("/quienes-somos")
    public String quienesSomos() {
        return "publico/quienes-somos";
    }

    @GetMapping("/personalizaciones")
    public String personalizaciones() {
        return "publico/personalizaciones";
    }

    @GetMapping("/politicas-privacidad")
    public String politicasPrivacidad() {
        return "publico/politicas-privacidad";
    }

    @GetMapping("/terminos-condiciones")
    public String terminosCondiciones() {
        return "publico/terminos-condiciones";
    }
}
