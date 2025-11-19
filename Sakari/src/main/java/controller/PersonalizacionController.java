package Sakari.controller;

import Sakari.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/personalizaciones")
@RequiredArgsConstructor
public class PersonalizacionController {

    private final ProductoService productoService;

    @GetMapping
    public String personalizaciones(Model model) {
        model.addAttribute("productosPersonalizables", productoService.listarPersonalizables());
        return "productos/personalizaciones";  // Cambio aquí
    }
}