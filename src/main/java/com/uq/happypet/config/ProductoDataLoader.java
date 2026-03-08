package com.uq.happypet.config;

import com.uq.happypet.model.Producto;
import com.uq.happypet.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Inserta productos iniciales y adicionales solo si no existen por nombre (no borra datos existentes).
 * Categorías: Alimentación, Higiene, Juguetes, Accesorios.
 */
@Configuration
public class ProductoDataLoader {

    @Bean
    public CommandLineRunner cargarProductosIniciales(ProductoRepository productoRepository) {
        return args -> {
            List<Producto> productos = List.of(
                    // --- Alimentación ---
                    p("Royal Canin Mini Adult 2kg", "Alimento seco premium para perros adultos de razas pequeñas.", "Alimentación", 95000, 20, "https://www.tierragro.com/cdn/shop/files/3182550793612_5afda598-88d4-4d90-8525-04ef3e874f1a_1800x1800.png?v=1743527471"),
                    p("Royal Canin Medium Adult 4kg", "Concentrado premium para perros adultos de razas medianas.", "Alimentación", 135000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111102978-v1-min.jpg"),
                    p("Royal Canin Maxi Adult 4kg", "Alimento balanceado para perros adultos de razas grandes.", "Alimentación", 142000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111100542-v1-min.jpg"),
                    p("Dog Chow Adultos 8kg", "Concentrado completo para perros adultos con proteínas de calidad.", "Alimentación", 78000, 25, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111100220_ed-min.jpg"),
                    p("Dog Chow Cachorros 8kg", "Alimento balanceado para cachorros en crecimiento.", "Alimentación", 82000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111103179_ed-min.jpg"),
                    p("Pedigree Adulto Raza Pequeña 4kg", "Alimento seco completo para perros adultos de razas pequeñas con proteínas y vitaminas esenciales.", "Alimentación", 58000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/912000040-min.jpg"),
                    p("Pedigree Cachorro 4kg", "Alimento balanceado para cachorros en crecimiento que ayuda al desarrollo de huesos y músculos.", "Alimentación", 62000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/912000041-min.jpg"),
                    p("Chunky Adultos Pollo y Arroz 9kg", "Alimento seco para perros adultos con pollo y arroz que favorece la digestión.", "Alimentación", 95000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/910000057-min.jpg"),
                    p("Chunky Adulto Vida Activa 8kg", "Alimento para perros adultos con alta actividad física que ayuda a mantener energía y vitalidad.", "Alimentación", 92000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/910000063-min.jpg"),
                    p("Chunky Adulto Razas Pequeñas Pollo y Arroz 8kg", "Concentrado especializado para perros adultos de razas pequeñas.", "Alimentación", 96000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/910000060-min.jpg"),
                    p("Chunky Cachorros Cordero Arroz y Salmón 8kg", "Alimento para cachorros con proteínas de alta calidad para un crecimiento saludable.", "Alimentación", 98000, 14, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/910000066-min.jpg"),
                    p("Max Cat Premium Gatos Castrados Pollo 3kg", "Alimento seco premium para gatos esterilizados con control de peso.", "Alimentación", 52000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/911000209-min.jpg"),
                    p("Max Cat Adultos Pollo y Arroz 3kg", "Alimento balanceado para gatos adultos con proteínas y nutrientes esenciales.", "Alimentación", 49000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/911000212-min.jpg"),
                    p("Max Cat Gatitos Pollo 1kg", "Alimento seco especialmente formulado para gatitos en crecimiento.", "Alimentación", 22000, 22, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/9/1/911000213-min.jpg"),
                    p("Hills Prescription Diet Digestive Care i/d 13oz", "Alimento húmedo veterinario para perros con problemas digestivos.", "Alimentación", 16000, 25, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111100077-v1-min.jpg"),
                    p("Nutra Nuggets Senior Pollo 15kg", "Alimento seco para perros senior con nutrientes que apoyan articulaciones y digestión.", "Alimentación", 210000, 10, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111100366_ed-min.jpg"),
                    p("Monge Vet Solution Gastrointestinal 150g", "Alimento húmedo veterinario para perros con sensibilidad digestiva.", "Alimentación", 13500, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111103321-v2-min.jpg"),
                    p("Pro Plan Wet Salmón Gatos 85g", "Alimento húmedo premium para gatos adultos con sabor a salmón.", "Alimentación", 6500, 40, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111103351_ed-min.jpg"),
                    p("Felix Paté Salmón 156g", "Alimento húmedo para gatos con textura tipo paté sabor salmón.", "Alimentación", 7000, 35, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111110352_ed-min.jpg"),
                    p("Royal Canin Kitten Lata 85g", "Alimento húmedo premium para gatitos en crecimiento.", "Alimentación", 8500, 35, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111111299_ed-min.jpg"),
                    p("Fancy Feast Atún y Salmón 85g", "Alimento húmedo gourmet para gatos adultos con sabor a atún y salmón.", "Alimentación", 6500, 35, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/111111481-min.jpg"),

                    // --- Higiene ---
                    p("Shampoo Keracleen 240ml", "Shampoo dermatológico para perros y gatos que ayuda a mantener la piel saludable.", "Higiene", 42000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/5/_5_7_5757457415741000031.jpg"),
                    p("Shampoo Baxidin 250ml", "Shampoo medicado para perros y gatos con acción antibacteriana.", "Higiene", 38000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/5/_5_7_5757457415741000328_1-min.jpg"),
                    p("Cepillo Slicker Large", "Cepillo para remover pelo muerto y desenredar el pelaje de perros y gatos.", "Higiene", 22000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/3/_3_1_313100091.jpg"),
                    p("Jabón Árbol de Té Mascotas 90g", "Jabón para perros y gatos con aceite de árbol de té que ayuda a limpiar y desinfectar.", "Higiene", 12000, 25, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/5/_5_7_5757457405740000003-min.jpg"),
                    p("CanAmor Baño Seco Talco Desodorante 100g", "Talco desodorante para mascotas que ayuda a mantener el pelaje limpio sin necesidad de agua.", "Higiene", 18000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/5/_5_7_5757457425742000004-min.jpg"),
                    p("Shampoo Petys Limpieza y Suavidad 235ml", "Shampoo suave para perros y gatos que limpia y mantiene el pelaje brillante.", "Higiene", 16000, 22, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/5/7/5757457415741000511_ed-min.jpg"),
                    p("Guante Removedor de Pelo Dog Grooming", "Guante de aseo que permite retirar el pelo muerto de perros y gatos.", "Higiene", 24000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/1/313100254-v1-min.jpg"),
                    p("Toallitas Húmedas Petys Clorhexidina 40und", "Pañitos húmedos con clorhexidina para la limpieza de mascotas.", "Higiene", 15000, 25, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/5/353520451-v1-min.jpg"),
                    p("Arena para Gatos Maxicat 10kg Aroma Bebé", "Arena sanitaria para gatos con aroma que ayuda a controlar olores.", "Higiene", 34000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/1/313120461-v1-min.jpg"),

                    // --- Juguetes (nuevos) ---
                    p("Chuckit Ultra Squeaker Ball Perro Talla S", "Pelota resistente para perros con sonido squeaker que estimula el juego y la actividad física.", "Juguetes", 28000, 25, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333304191-min.jpg"),
                    p("Pelota Dispensadora de Snacks para Perros 11cm", "Pelota interactiva que dispensa alimento o snacks mientras el perro juega.", "Juguetes", 24000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/3/_3_3_333310072-_4__ed-min.jpg"),
                    p("Hueso Dental para Perros Ferplast", "Juguete mordedor diseñado para fortalecer dientes y encías en perros.", "Juguetes", 22000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333311039-min.jpg"),
                    p("Peluche Alpaca para Perros", "Juguete de peluche suave para perros ideal para juegos de compañía.", "Juguetes", 26000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333303245-min.jpg"),
                    p("Peluche León Gigwi Plush Friendz", "Juguete de peluche con caucho termoplástico resistente para perros.", "Juguetes", 32000, 16, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333304250-min.jpg"),
                    p("Peluche Alpaca para Perro 22cm", "Juguete de peluche resistente para perros ideal para juego interactivo.", "Juguetes", 27000, 16, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333310891_1_-min.jpg"),
                    p("Peluche Hippo Suppa Puppa", "Juguete de peluche con sonido para estimular el juego en perros.", "Juguetes", 29000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333303870-min.jpg"),
                    p("Juguete Arcoíris para Gatos", "Juguete interactivo para gatos que estimula el instinto de caza.", "Juguetes", 14000, 30, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/_/3/_3_3_333301088.jpg"),
                    p("Pelota Espiral para Gatos 4.5cm", "Pelota ligera para gatos ideal para juegos de persecución.", "Juguetes", 9000, 35, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333303916-min.jpg"),
                    p("Pelota Erizo Trixie para Gatos", "Juguete tipo erizo con textura para estimular el juego en gatos.", "Juguetes", 11000, 30, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333310953-min.jpg"),
                    p("Ratón de Cuerda para Gatos Trixie", "Juguete para gatos con forma de ratón ideal para estimular la caza.", "Juguetes", 10000, 30, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/3/333301001-min.jpg"),

                    // --- Accesorios (nuevos) ---
                    p("Arnés Trekking para Perros Trixie S-M", "Arnés cómodo y seguro para paseos con perros.", "Accesorios", 42000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323215584-min.jpg"),
                    p("Arnés Pechera Toby Totto Pets Talla M", "Arnés para perros con diseño cómodo y resistente.", "Accesorios", 46000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323215187-min.jpg"),
                    p("Correa para Perros Animal Factor Azul", "Correa resistente para paseos seguros con mascotas.", "Accesorios", 25000, 18, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323213492-min.jpg"),
                    p("Arnés Lona Premium para Perros XL", "Arnés resistente diseñado para mayor control y seguridad.", "Accesorios", 45000, 12, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/1/1/119000114-min.jpg"),
                    p("Placa de Identificación para Mascotas Azul", "Placa de identificación para colocar información de la mascota.", "Accesorios", 15000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323240220-min.jpg"),
                    p("Placa de Identificación Shine Negra", "Placa metálica para identificación de mascotas.", "Accesorios", 16000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323240510-min.jpg"),
                    p("Collar Reflectivo para Gato Trixie", "Collar reflectivo con broche de seguridad para gatos.", "Accesorios", 18000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323212453-min.jpg"),
                    p("Collar Kitty Cat para Gatos", "Collar ajustable para gatos con diseño cómodo.", "Accesorios", 16000, 20, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323210235-min.jpg"),
                    p("Arnés para Gato Trixie 24-42cm", "Arnés ajustable para gatos con correa incluida.", "Accesorios", 32000, 15, "https://www.agrocampo.com.co/media/catalog/product/cache/dd0974c17aa11c1008feb0c4f8e4080c/3/2/323212629-min.jpg")
            );

            for (Producto producto : productos) {
                if (!productoRepository.existsByNombreIgnoreCase(producto.getNombre())) {
                    productoRepository.save(producto);
                }
            }
        };
    }

    private static Producto p(String nombre, String descripcion, String categoria, double precio, int stock, String imagenUrl) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);
        return producto;
    }
}
