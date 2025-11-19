package Sakari.controller;

import Sakari.domain.Categoria;
import Sakari.service.CategoriaService;
import Sakari.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String listarProductos(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String busqueda,
            Model model) {
        
        if (categoriaId != null) {
            Categoria categoria = categoriaService.buscarPorId(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            model.addAttribute("productos", productoService.listarPorCategoria(categoria));
            model.addAttribute("categoriaSeleccionada", categoria);
        } else if (busqueda != null && !busqueda.isEmpty()) {
            model.addAttribute("productos", productoService.buscarPorPalabraClave(busqueda));
            model.addAttribute("busqueda", busqueda);
        } else {
            model.addAttribute("productos", productoService.listarActivos());
        }
        
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "productos/lista";  // Cambio aquí
    }

    @GetMapping("/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
        return "productos/detalle";  // Cambio aquí
    }
}