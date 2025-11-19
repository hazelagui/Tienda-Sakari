package Sakari.controller;

import Sakari.security.CustomUserDetails;
import Sakari.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public String verCarrito(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long usuarioId = userDetails.getId();
        
        try {
            model.addAttribute("carrito", carritoService.obtenerCarritoPorUsuarioId(usuarioId));
            model.addAttribute("total", carritoService.calcularTotal(usuarioId));
        } catch (RuntimeException e) {
            model.addAttribute("mensaje", "Error al cargar el carrito");
        }
        
        return "carrito/carrito";  // Cambio aquí
    }

    @PostMapping("/agregar")
    public String agregarProducto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(required = false) String personalizacion,
            RedirectAttributes redirectAttributes) {
        
        Long usuarioId = userDetails.getId();
        
        try {
            carritoService.agregarProducto(usuarioId, productoId, cantidad, personalizacion);
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/carrito";
    }

    @PostMapping("/actualizar/{itemId}")
    public String actualizarCantidad(
            @PathVariable Long itemId,
            @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {
        
        try {
            carritoService.actualizarCantidad(itemId, cantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar/{itemId}")
    public String eliminarItem(
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {
        
        try {
            carritoService.eliminarItem(itemId);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/carrito";
    }

    @PostMapping("/limpiar")
    public String limpiarCarrito(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        Long usuarioId = userDetails.getId();
        
        try {
            carritoService.limpiarCarrito(usuarioId);
            redirectAttributes.addFlashAttribute("mensaje", "Carrito limpiado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/carrito";
    }
}
