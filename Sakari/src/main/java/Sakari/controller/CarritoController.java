package Sakari.controller;

import Sakari.domain.Carrito;
import Sakari.domain.Usuario;
import Sakari.security.CustomUserDetails;
import Sakari.service.CarritoService;
import Sakari.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String verCarrito(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
        Carrito carrito = carritoService.obtenerOCrearCarrito(usuario);
        
        BigDecimal total = carritoService.calcularTotal(carrito);
        
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        
        return "carrito/index";
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Integer> contarItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(0);
        }
        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            Carrito carrito = carritoService.obtenerOCrearCarrito(usuario);
            int count = carrito.getItems() != null ? carrito.getItems().size() : 0;
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    @PostMapping("/agregar/{productoId}")
    public String agregarProductoPorPath(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(required = false) String personalizacion,
            RedirectAttributes redirectAttributes) {
        
        return agregarProducto(userDetails, productoId, cantidad, personalizacion, redirectAttributes);
    }

    @PostMapping("/agregar")
    public String agregarProducto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(required = false) String personalizacion,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            carritoService.agregarProducto(usuario, productoId, cantidad, personalizacion);
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
            log.info("Producto {} agregado al carrito del usuario {}", productoId, usuario.getEmail());
            
        } catch (RuntimeException e) {
            log.error("Error al agregar producto al carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizarCantidad(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long itemId,
            @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            
            if (cantidad <= 0) {
                carritoService.eliminarItem(usuario, itemId);
                redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
            } else {
                carritoService.actualizarCantidad(usuario, itemId, cantidad);
                redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada");
            }
            
        } catch (RuntimeException e) {
            log.error("Error al actualizar carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/carrito";
    }

    @PostMapping("/eliminar/{itemId}")
    public String eliminarItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            carritoService.eliminarItem(usuario, itemId);
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
            
        } catch (RuntimeException e) {
            log.error("Error al eliminar item del carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/carrito";
    }

    @PostMapping("/limpiar")
    public String limpiarCarrito(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            carritoService.limpiarCarrito(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", "Carrito vaciado");
            
        } catch (RuntimeException e) {
            log.error("Error al limpiar carrito: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/carrito";
    }

    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
        Carrito carrito = carritoService.obtenerOCrearCarrito(usuario);
        
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            return "redirect:/carrito";
        }
        
        BigDecimal total = carritoService.calcularTotal(carrito);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        
        return "carrito/checkout";
    }

    @PostMapping("/procesar-pedido")
    public String procesarPedido(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String notas,
            @RequestParam String metodoPago,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorId(userDetails.getId());
            Carrito carrito = carritoService.obtenerOCrearCarrito(usuario);
            
            if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/carrito";
            }

            // Actualizar datos del usuario si es necesario
            if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty()) {
                usuario.setTelefono(telefono);
            }
            if (usuario.getDireccion() == null || usuario.getDireccion().isEmpty()) {
                usuario.setDireccion(direccion);
            }
            usuarioService.actualizarUsuario(usuario);

            // Por ahora solo limpiar carrito y mostrar confirmación
            // En producción aquí se crearía el pedido en base de datos
            BigDecimal total = carritoService.calcularTotal(carrito);
            int cantidadItems = carrito.getItems().size();
            
            carritoService.limpiarCarrito(usuario);
            
            log.info("Pedido procesado para usuario {}: {} items, total {} ₡, método: {}", 
                    usuario.getEmail(), cantidadItems, total, metodoPago);
            
            redirectAttributes.addFlashAttribute("mensaje", 
                "¡Pedido confirmado! Te contactaremos pronto para coordinar el pago y envío.");
            redirectAttributes.addFlashAttribute("pedidoTotal", total);
            redirectAttributes.addFlashAttribute("metodoPago", metodoPago);
            
            return "redirect:/carrito/confirmacion";
            
        } catch (RuntimeException e) {
            log.error("Error al procesar pedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/carrito/checkout";
        }
    }

    @GetMapping("/confirmacion")
    public String confirmacion(Model model) {
        return "carrito/confirmacion";
    }
}
