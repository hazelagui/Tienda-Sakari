package Sakari.controller;

import Sakari.domain.Producto;
import Sakari.service.ProductoService;
import Sakari.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String listarProductos(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String buscar,
            Model model) {

        List<Producto> productos;

        if (buscar != null && !buscar.trim().isEmpty()) {
            productos = productoService.buscarPorNombre(buscar.trim());
        } else if (categoria != null) {
            productos = productoService.listarPorCategoria(categoria);
        } else {
            productos = productoService.listarActivos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("busqueda", buscar);

        return "productos/lista";
    }

    @GetMapping("/{id}")
    public String verProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id);
        
        if (producto == null || !producto.getActivo()) {
            return "redirect:/productos";
        }

        model.addAttribute("producto", producto);
        
        // Productos relacionados de la misma categoría
        if (producto.getCategoria() != null) {
            List<Producto> relacionados = productoService.listarPorCategoria(producto.getCategoria().getId());
            relacionados.removeIf(p -> p.getId().equals(id));
            if (relacionados.size() > 4) {
                relacionados = relacionados.subList(0, 4);
            }
            model.addAttribute("productosRelacionados", relacionados);
        }

        return "productos/detalle";
    }
}
