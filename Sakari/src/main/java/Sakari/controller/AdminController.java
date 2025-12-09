package Sakari.controller;

import Sakari.domain.*;
import Sakari.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final ContactoService contactoService;

    // ==================== DASHBOARD ====================
    
    @GetMapping
    public String dashboard(Model model) {
        // Estadísticas generales
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", productoService.listarTodos().size());
        stats.put("totalUsuarios", usuarioService.listarTodos().size());
        stats.put("totalPedidos", pedidoService.listarTodos().size());
        stats.put("mensajesNoLeidos", contactoService.contarNoLeidos());
        
        // Pedidos recientes
        var pedidosRecientes = pedidoService.listarTodos().stream()
                .limit(5)
                .toList();
        
        model.addAttribute("stats", stats);
        model.addAttribute("pedidosRecientes", pedidosRecientes);
        model.addAttribute("categorias", categoriaService.listarTodas());
        
        return "admin/dashboard";
    }

    // ==================== PRODUCTOS ====================
    
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/productos/lista";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/productos/formulario";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam BigDecimal precio,
            @RequestParam Integer stock,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String imagenUrl,
            @RequestParam(defaultValue = "false") Boolean personalizable,
            @RequestParam(defaultValue = "true") Boolean activo,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Producto producto;
            if (id != null) {
                producto = productoService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            } else {
                producto = new Producto();
            }
            
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setImagenUrl(imagenUrl);
            producto.setPersonalizable(personalizable);
            producto.setActivo(activo);
            
            if (categoriaId != null) {
                Categoria categoria = categoriaService.buscarPorId(categoriaId)
                        .orElse(null);
                producto.setCategoria(categoria);
            }
            
            if (id != null) {
                productoService.actualizarProducto(producto);
                redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            } else {
                productoService.crearProducto(producto);
                redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            }
            
            log.info("Producto guardado: {}", nombre);
            
        } catch (Exception e) {
            log.error("Error al guardar producto", e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
        }
        
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProductoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaService.listarTodas());
            return "admin/productos/formulario";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/admin/productos";
        }
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            log.info("Producto eliminado: {}", id);
        } catch (Exception e) {
            log.error("Error al eliminar producto", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/nuevo")
    public String crearProductoNuevo(@ModelAttribute Producto producto,
                                     @RequestParam(required = false) Long categoriaId,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (categoriaId != null) {
                Categoria categoria = categoriaService.buscarPorId(categoriaId).orElse(null);
                producto.setCategoria(categoria);
            }
            if (producto.getActivo() == null) producto.setActivo(false);
            if (producto.getPersonalizable() == null) producto.setPersonalizable(false);
            
            productoService.crearProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            log.info("Producto creado: {}", producto.getNombre());
        } catch (Exception e) {
            log.error("Error al crear producto", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/editar/{id}")
    public String actualizarProductoExistente(@PathVariable Long id,
                                              @ModelAttribute Producto producto,
                                              @RequestParam(required = false) Long categoriaId,
                                              RedirectAttributes redirectAttributes) {
        try {
            Producto existente = productoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            existente.setNombre(producto.getNombre());
            existente.setDescripcion(producto.getDescripcion());
            existente.setPrecio(producto.getPrecio());
            existente.setStock(producto.getStock());
            existente.setImagenUrl(producto.getImagenUrl());
            existente.setPersonalizable(producto.getPersonalizable() != null ? producto.getPersonalizable() : false);
            existente.setActivo(producto.getActivo() != null ? producto.getActivo() : false);
            
            if (categoriaId != null) {
                Categoria categoria = categoriaService.buscarPorId(categoriaId).orElse(null);
                existente.setCategoria(categoria);
            }
            
            productoService.actualizarProducto(existente);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            log.info("Producto actualizado: {}", producto.getNombre());
        } catch (Exception e) {
            log.error("Error al actualizar producto", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/toggle-activo/{id}")
    public String toggleActivoProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            producto.setActivo(!producto.getActivo());
            productoService.actualizarProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", 
                    producto.getActivo() ? "Producto activado" : "Producto desactivado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado del producto");
        }
        return "redirect:/admin/productos";
    }

    // ==================== CATEGORÍAS ====================
    
    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/categorias/lista";
    }

    @GetMapping("/categorias/nueva")
    public String nuevaCategoriaForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/formulario";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(defaultValue = "true") Boolean activo,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Categoria categoria;
            if (id != null) {
                categoria = categoriaService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            } else {
                categoria = new Categoria();
            }
            
            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);
            categoria.setActivo(activo);
            
            if (id != null) {
                categoriaService.actualizarCategoria(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada exitosamente");
            } else {
                categoriaService.crearCategoria(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría creada exitosamente");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la categoría: " + e.getMessage());
        }
        
        return "redirect:/admin/categorias";
    }

    @GetMapping("/categorias/editar/{id}")
    public String editarCategoriaForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            model.addAttribute("categoria", categoria);
            return "admin/categorias/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
            return "redirect:/admin/categorias";
        }
    }

    @PostMapping("/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.eliminarCategoria(id);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la categoría");
        }
        return "redirect:/admin/categorias";
    }

    // ==================== PEDIDOS ====================
    
    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(required = false) String estado,
            Model model) {
        
        if (estado != null && !estado.isEmpty()) {
            try {
                Pedido.EstadoPedido estadoPedido = Pedido.EstadoPedido.valueOf(estado);
                model.addAttribute("pedidos", pedidoService.listarTodos().stream()
                        .filter(p -> p.getEstado() == estadoPedido)
                        .toList());
            } catch (IllegalArgumentException e) {
                model.addAttribute("pedidos", pedidoService.listarTodos());
            }
        } else {
            model.addAttribute("pedidos", pedidoService.listarTodos());
        }
        
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("estadoSeleccionado", estado);
        
        return "admin/pedidos/lista";
    }

    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            model.addAttribute("pedido", pedido);
            model.addAttribute("estados", Pedido.EstadoPedido.values());
            return "admin/pedidos/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
            return "redirect:/admin/pedidos";
        }
    }

    @PostMapping("/pedidos/{id}/estado")
    public String actualizarEstadoPedido(
            @PathVariable Long id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {
        
        try {
            Pedido.EstadoPedido nuevoEstado = Pedido.EstadoPedido.valueOf(estado);
            pedidoService.actualizarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del pedido actualizado");
            log.info("Pedido {} actualizado a estado: {}", id, estado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado");
        }
        
        return "redirect:/admin/pedidos/" + id;
    }

    // ==================== USUARIOS ====================
    
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios/lista";
    }

    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedidos", pedidoService.listarPedidosUsuario(id));
            return "admin/usuarios/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/{id}/toggle-activo")
    public String toggleActivoUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setActivo(!usuario.getActivo());
            usuarioService.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", 
                    usuario.getActivo() ? "Usuario activado" : "Usuario desactivado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado del usuario");
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRolUsuario(
            @PathVariable Long id,
            @RequestParam String rol,
            RedirectAttributes redirectAttributes) {
        
        try {
            Usuario usuario = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setRol(rol);
            usuarioService.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado exitosamente");
            log.info("Rol de usuario {} cambiado a: {}", usuario.getEmail(), rol);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el rol");
        }
        
        return "redirect:/admin/usuarios/" + id;
    }

    // ==================== MENSAJES DE CONTACTO ====================
    
    @GetMapping("/mensajes")
    public String listarMensajes(Model model) {
        model.addAttribute("mensajes", contactoService.listarTodos());
        model.addAttribute("noLeidos", contactoService.contarNoLeidos());
        return "admin/mensajes/lista";
    }

    @GetMapping("/mensajes/{id}")
    public String verMensaje(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MensajeContacto mensaje = contactoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
            
            // Marcar como leído
            if (!mensaje.getLeido()) {
                contactoService.marcarComoLeido(id);
                mensaje.setLeido(true);
            }
            
            model.addAttribute("mensaje", mensaje);
            return "admin/mensajes/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Mensaje no encontrado");
            return "redirect:/admin/mensajes";
        }
    }

    @PostMapping("/mensajes/{id}/respondido")
    public String marcarRespondido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactoService.marcarComoRespondido(id);
            redirectAttributes.addFlashAttribute("mensaje", "Mensaje marcado como respondido");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al marcar el mensaje");
        }
        return "redirect:/admin/mensajes";
    }

    @PostMapping("/mensajes/{id}/eliminar")
    public String eliminarMensaje(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactoService.eliminarMensaje(id);
            redirectAttributes.addFlashAttribute("mensaje", "Mensaje eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el mensaje");
        }
        return "redirect:/admin/mensajes";
    }

    @PostMapping("/mensajes/eliminar/{id}")
    public String eliminarMensajeAlt(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactoService.eliminarMensaje(id);
            redirectAttributes.addFlashAttribute("mensaje", "Mensaje eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el mensaje");
        }
        return "redirect:/admin/mensajes";
    }
}
