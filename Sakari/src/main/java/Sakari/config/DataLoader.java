package Sakari.config;

import Sakari.domain.Categoria;
import Sakari.domain.Producto;
import Sakari.domain.Usuario;
import Sakari.repository.CategoriaRepository;
import Sakari.repository.ProductoRepository;
import Sakari.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("===========================================");
        log.info("SAKARI - Iniciando carga de datos...");
        log.info("===========================================");
        
        // Siempre verificar y crear usuarios admin
        cargarUsuarios();
        
        // Cargar categorías si no existen
        if (categoriaRepository.count() == 0) {
            log.info("Cargando categorías...");
            cargarCategorias();
        } else {
            log.info("Categorías existentes: {}", categoriaRepository.count());
        }
        
        // Cargar productos si no existen
        if (productoRepository.count() == 0) {
            log.info("Cargando productos...");
            cargarProductos();
        } else {
            log.info("Productos existentes: {}", productoRepository.count());
        }
        
        log.info("===========================================");
        log.info("SAKARI - Datos cargados exitosamente!");
        log.info("Usuarios: {}", usuarioRepository.count());
        log.info("Categorías: {}", categoriaRepository.count());
        log.info("Productos: {}", productoRepository.count());
        log.info("===========================================");
    }

    private void cargarCategorias() {
        Categoria camisetas = new Categoria();
        camisetas.setNombre("Camisetas");
        camisetas.setDescripcion("Camisetas de poliéster sublimables en variedad de tallas");
        camisetas.setActivo(true);
        categoriaRepository.save(camisetas);

        Categoria tazas = new Categoria();
        tazas.setNombre("Tazas");
        tazas.setDescripcion("Tazas de cerámica con recubrimiento especial para sublimación");
        tazas.setActivo(true);
        categoriaRepository.save(tazas);

        Categoria gorras = new Categoria();
        gorras.setNombre("Gorras");
        gorras.setDescripcion("Gorras sublimables de diferentes estilos y colores");
        gorras.setActivo(true);
        categoriaRepository.save(gorras);

        Categoria cojines = new Categoria();
        cojines.setNombre("Cojines");
        cojines.setDescripcion("Cojines decorativos con fundas sublimables");
        cojines.setActivo(true);
        categoriaRepository.save(cojines);

        Categoria llaveros = new Categoria();
        llaveros.setNombre("Llaveros");
        llaveros.setDescripcion("Llaveros personalizables con diferentes formas");
        llaveros.setActivo(true);
        categoriaRepository.save(llaveros);

        Categoria otros = new Categoria();
        otros.setNombre("Otros");
        otros.setDescripcion("Otros productos sublimables");
        otros.setActivo(true);
        categoriaRepository.save(otros);
        
        log.info("6 categorías cargadas");
    }

    private void cargarProductos() {
        Categoria camisetas = categoriaRepository.findByNombre("Camisetas").orElse(null);
        Categoria tazas = categoriaRepository.findByNombre("Tazas").orElse(null);
        Categoria gorras = categoriaRepository.findByNombre("Gorras").orElse(null);
        Categoria cojines = categoriaRepository.findByNombre("Cojines").orElse(null);
        Categoria llaveros = categoriaRepository.findByNombre("Llaveros").orElse(null);
        Categoria otros = categoriaRepository.findByNombre("Otros").orElse(null);

        // Producto 1 - Camiseta Blanca
        Producto p1 = new Producto();
        p1.setNombre("Camiseta Blanca Premium");
        p1.setDescripcion("Camiseta 100% poliéster blanca de alta calidad, ideal para sublimación full color. Disponible en tallas S, M, L, XL. Acabado suave y cómodo.");
        p1.setPrecio(new BigDecimal("8500.00"));
        p1.setStock(50);
        p1.setImagenUrl("https://images-na.ssl-images-amazon.com/images/I/51UTLAXYz8L._AC_UL600_SR600,600_.jpg");
        p1.setPersonalizable(true);
        p1.setActivo(true);
        p1.setCategoria(camisetas);
        p1.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p1);

        // Producto 2 - Camiseta Negra
        Producto p2 = new Producto();
        p2.setNombre("Camiseta Negra Sport");
        p2.setDescripcion("Camiseta deportiva de poliéster negro con zonas sublimables. Perfecta para equipos deportivos. Tallas S-XL.");
        p2.setPrecio(new BigDecimal("9500.00"));
        p2.setStock(30);
        p2.setImagenUrl("https://www.shutterstock.com/image-vector/blank-tee-shirt-vector-mockup-600nw-2475398061.jpg");
        p2.setPersonalizable(true);
        p2.setActivo(true);
        p2.setCategoria(camisetas);
        p2.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p2);

        // Producto 3 - Taza Mágica
        Producto p3 = new Producto();
        p3.setNombre("Taza Mágica Térmica");
        p3.setDescripcion("Taza mágica que revela tu diseño con el calor. Sorprende a todos con este efecto especial. Capacidad 11oz.");
        p3.setPrecio(new BigDecimal("6500.00"));
        p3.setStock(40);
        p3.setImagenUrl("https://tiendadepromocionales.com/cdn/shop/products/TAZAMAGICAPARASUBLIMAR2_800x.jpg?v=1624036275");
        p3.setPersonalizable(true);
        p3.setActivo(true);
        p3.setCategoria(tazas);
        p3.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p3);

        // Producto 4 - Taza Blanca
        Producto p4 = new Producto();
        p4.setNombre("Taza Blanca Clásica");
        p4.setDescripcion("Taza de cerámica premium 11oz, superficie perfecta para sublimación de alta definición.");
        p4.setPrecio(new BigDecimal("4500.00"));
        p4.setStock(100);
        p4.setImagenUrl("https://m.media-amazon.com/images/I/41yI4AC5PGL._AC_UF894,1000_QL80_.jpg");
        p4.setPersonalizable(true);
        p4.setActivo(true);
        p4.setCategoria(tazas);
        p4.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p4);

        // Producto 5 - Gorra
        Producto p5 = new Producto();
        p5.setNombre("Gorra Trucker Original");
        p5.setDescripcion("Gorra estilo trucker con panel frontal sublimable. Ajustable con broche trasero. Máxima comodidad.");
        p5.setPrecio(new BigDecimal("7000.00"));
        p5.setStock(25);
        p5.setImagenUrl("https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400&h=400&fit=crop");
        p5.setPersonalizable(true);
        p5.setActivo(true);
        p5.setCategoria(gorras);
        p5.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p5);

        // Producto 6 - Cojín
        Producto p6 = new Producto();
        p6.setNombre("Cojín");
        p6.setDescripcion("Cojín con funda sublimable 40x40cm. Incluye relleno de alta calidad. Perfecto para decorar cualquier espacio.");
        p6.setPrecio(new BigDecimal("12000.00"));
        p6.setStock(20);
        p6.setImagenUrl("https://i.pinimg.com/originals/86/f4/6e/86f46e694745bdb4d7a21fd2bdc6d3fa.png");
        p6.setPersonalizable(true);
        p6.setActivo(true);
        p6.setCategoria(cojines);
        p6.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p6);

        // Producto 7 - Llavero
        Producto p7 = new Producto();
        p7.setNombre("Llavero Metálico");
        p7.setDescripcion("Llavero de metal cromado con área sublimable. Duradero y elegante. Ideal para regalos personalizados.");
        p7.setPrecio(new BigDecimal("2500.00"));
        p7.setStock(75);
        p7.setImagenUrl("https://tiendatransfer.com/5144-large_default/llavero-metalico-redondo.jpg");
        p7.setPersonalizable(true);
        p7.setActivo(true);
        p7.setCategoria(llaveros);
        p7.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p7);

        // Producto 8 - Mousepad
        Producto p8 = new Producto();
        p8.setNombre("Mousepad");
        p8.setDescripcion("Mousepad con superficie de tela sublimable de alta precisión. Tamaño 30x25cm. Base antideslizante.");
        p8.setPrecio(new BigDecimal("5500.00"));
        p8.setStock(35);
        p8.setImagenUrl("https://www.serigraficos.com/wp-content/uploads/2021/12/01-2.jpg");
        p8.setPersonalizable(true);
        p8.setActivo(true);
        p8.setCategoria(otros);
        p8.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p8);

        // Producto 9 - Bolso
        Producto p9 = new Producto();
        p9.setNombre("Bolso Tote Canvas");
        p9.setDescripcion("Bolso tipo tote de canvas resistente con amplia área sublimable. Ecológico y reutilizable.");
        p9.setPrecio(new BigDecimal("8000.00"));
        p9.setStock(15);
        p9.setImagenUrl("https://m.media-amazon.com/images/I/61TRnaxSBaL._AC_SL1500_.jpg");
        p9.setPersonalizable(true);
        p9.setActivo(true);
        p9.setCategoria(otros);
        p9.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p9);

        // Producto 10 - Rompecabezas
        Producto p10 = new Producto();
        p10.setNombre("Rompecabezas Fotográfico");
        p10.setDescripcion("Rompecabezas sublimable tamaño A4 con 120 piezas. Transforma tus fotos en un regalo único y divertido.");
        p10.setPrecio(new BigDecimal("7500.00"));
        p10.setStock(30);
        p10.setImagenUrl("https://www.copiroyal.com/wp-content/uploads/2016/10/copiroyal-soluciones-grafing-detalleria-rompecabezas.jpg");
        p10.setPersonalizable(true);
        p10.setActivo(true);
        p10.setCategoria(otros);
        p10.setFechaCreacion(LocalDateTime.now());
        productoRepository.save(p10);

        log.info("10 productos cargados");
    }

    private void cargarUsuarios() {
        // Usuario Administrador - Siempre verificar y crear/actualizar
        Usuario adminExistente = usuarioRepository.findByEmail("admin@sakari.com").orElse(null);
        
        if (adminExistente == null) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("Sakari");
            admin.setEmail("admin@sakari.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setTelefono("88735743");
            admin.setDireccion("Heredia, Costa Rica");
            admin.setRol("ADMIN");
            admin.setActivo(true);
            admin.setFechaRegistro(LocalDateTime.now());
            usuarioRepository.save(admin);
            log.info("✅ Usuario ADMIN creado: admin@sakari.com / admin123");
        } else {
            // Actualizar contraseña del admin por si acaso
            adminExistente.setPassword(passwordEncoder.encode("admin123"));
            adminExistente.setActivo(true);
            adminExistente.setRol("ADMIN");
            usuarioRepository.save(adminExistente);
            log.info("✅ Usuario ADMIN actualizado: admin@sakari.com / admin123");
        }

        // Usuario de Prueba
        Usuario userExistente = usuarioRepository.findByEmail("usuario@test.com").orElse(null);
        
        if (userExistente == null) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Usuario");
            usuario.setApellido("Prueba");
            usuario.setEmail("usuario@test.com");
            usuario.setPassword(passwordEncoder.encode("user123"));
            usuario.setTelefono("88888888");
            usuario.setDireccion("Heredia, Costa Rica");
            usuario.setRol("USER");
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuarioRepository.save(usuario);
            log.info("✅ Usuario de prueba creado: usuario@test.com / user123");
        } else {
            // Actualizar contraseña del usuario por si acaso
            userExistente.setPassword(passwordEncoder.encode("user123"));
            userExistente.setActivo(true);
            usuarioRepository.save(userExistente);
            log.info("✅ Usuario de prueba actualizado: usuario@test.com / user123");
        }
    }
}
