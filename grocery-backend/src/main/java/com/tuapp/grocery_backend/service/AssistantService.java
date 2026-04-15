package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;
import java.text.Normalizer;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class AssistantService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private RecipeService recipeService;

    private Map<String, Producto> productoPendienteMap = new ConcurrentHashMap<>();

    public String chat(String message, String usuario) {

        if (usuario == null || usuario.isBlank()) {
            return "⚠️ Usuario no identificado";
        }

        if (message == null || message.isBlank()) {
            return "¿Puedes escribirlo otra vez? 😊";
        }

        String lower = message.toLowerCase();
        
        // 🔥 detectar ingrediente específico
        String ingredienteDetectado = null;

        if (message.toLowerCase().contains("receta con")) {

            String[] partes = message.toLowerCase().split("receta con");

            if (partes.length > 1) {
                ingredienteDetectado = partes[1].trim();
            }
        }

        // =========================
        // INSTRUCCIONES DE RECETA buscarRecetas
        // =========================
        if (lower.equals("si") || lower.equals("sí") || lower.contains("instrucciones")) {
            return recipeService.obtenerInstrucciones(usuario);
        }

        // =========================
        // RECETAS
        // =========================
        if (lower.contains("receta") ||
            lower.contains("qué puedo cocinar") ||
            lower.contains("que puedo cocinar") ||
            lower.contains("qué puedo hacer") ||
            lower.contains("que puedo hacer") ||
            lower.contains("recomiendame") ||
            lower.contains("recomiéndame")) {

            List<Map<String, Object>> carritoData = carritoService.obtenerCarrito(usuario);

            // 🔥 convertir carrito a lista de ingredientes reales
            List<String> carrito = new ArrayList<>();

            for (Map<String, Object> item : carritoData) {
                Object nombre = item.get("nombre");
                if (nombre != null) {
                    carrito.add(nombre.toString());
                }
            }

            // 🔥 si usuario pidió ingrediente específico
            if (ingredienteDetectado != null && !ingredienteDetectado.isEmpty()) {

                List<String> filtro = new ArrayList<>();
                filtro.add(ingredienteDetectado);

                return recipeService.buscarRecetas(filtro, usuario);

            } else {

                return recipeService.buscarRecetas(carrito, usuario);
            }
        }

        String msg = normalizar(message);

        List<Producto> productos = productoRepository.findAll();

        final String msgFinal = msg;

        productos.sort((a, b) -> Integer.compare(
                scoreProducto(msgFinal, b),
                scoreProducto(msgFinal, a)
        ));

        // =========================
        // SALUDO
        // =========================
        if (msg.matches(".*(hola+|holi+|buenas+|hey+|hi+|hello+).*")) {
            return "¡Hola! 👋\n\n"
            + "Puedo ayudarte a:\n"
            + "• Buscar productos 🛒\n"
            + "• Agregar cosas a tu carrito\n"
            + "• Recomendar recetas 🍳\n\n"
            + "¿Qué te gustaría hacer?";
        }

        // =========================
        // AYUDA
        // =========================
        if (msg.matches(".*(ayuda|help|ayudames|ayudame|ayudarme|ayudrme|que puedes hacer|qué puedes hacer|opciones|menu|menú).*")) {
            return "Claro 😊 puedo ayudarte con varias cosas:\n"
                + "- Buscar productos disponibles\n"
                + "- Agregar artículos al carrito\n"
                + "- Sugerirte recetas con ingredientes de tu carrito\n\n"
                + "Por ejemplo, puedes escribir:\n"
                + "- \"agrega 2 manzanas\"\n"
                + "- \"¿tienen pera?\"\n"
                + "- \"quiero una receta con manzana\"";
        }
        
        // =========================
        // BIENESTAR ALIMENTARIO (NUEVO MÓDULO APARTE)
        // =========================
        String respuestaBienestar = responderBienestar(msg);

        if (respuestaBienestar != null) {
            return respuestaBienestar;
        }
        
        // ====================
        // DESPEDIDA
        // =========================
        if (msg.matches(".*\\b(no|gracias|seria todo|gracias|nada|eso es todo|ya no)\\b.*")) {
            productoPendienteMap.remove(usuario);
            return "¡Perfecto! 😊 Muchas gracias por tu pedido 🛒";
        }

        msg = limpiarMensaje(msg);

        // =========================
        // VARIEDADES
        // =========================
        if (msg.matches(".*\\b(tipo|variedad|que tipo|tipos|cuales)\\b.*")) {

            for (Producto p : productos) {
                if (msg.contains(normalizar(p.getNombre()))) {

                    List<Producto> encontrados =
                            productoRepository.findByNombreContainingIgnoreCase(p.getNombre());

                    if (encontrados == null || encontrados.isEmpty()) {
                        return "No encontré variedades de " + capitalize(p.getNombre());
                    }

                    StringBuilder res = new StringBuilder("Tenemos:\n");

                    for (Producto prod : encontrados) {
                        res.append("- ")
                           .append(prod.getVariedad())
                           .append(" ($")
                           .append(prod.getPrecio())
                           .append(")\n");
                    }

                    return res.toString();
                }
            }
        }

        // =========================
        // DISPONIBILIDAD
        // =========================
        if (msg.matches(".*\\b(tienes|hay|vendes)\\b.*")) {

            productoPendienteMap.remove(usuario);

            for (Producto p : productos) {
                if (coincideProducto(msg, p)) {
                    productoPendienteMap.put(usuario, p);
                    return "Sí 😊 tenemos " + getNombre(p) + ". ¿Cuántos quieres?";
                }
            }

            return "Ups 😕 no encontré ese producto";
        }

        // =========================
        // INTENTO DE AGREGAR GENÉRICO (NUEVO)
        // =========================
        if (msg.matches(".*\\b(agrega|añade|pon|compra)\\b.*")) {

            for (Producto p : productos) {
                if (coincideProducto(msg, p)) {

                    double cantidad = detectarCantidadNumero(msg);

                    if (cantidad > 0) {
                        return agregarAlCarrito(p, cantidad, getNombre(p), usuario)
                                + " 🛒 ¿Quieres algo más?";
                    }

                    productoPendienteMap.put(usuario, p);
                    return "Claro 😊 tengo " + getNombre(p) + ", ¿cuántos deseas?";
                }
            }

            return "No encontré ese producto 😕 ¿puedes decirme el nombre exacto?";
        }

        // =========================
        // PRODUCTO PENDIENTE
        // =========================
        Producto pendiente = productoPendienteMap.get(usuario);

        if (pendiente != null) {

            double cantidad = detectarCantidadNumero(msg);

            if (cantidad > 0) {

                String res = agregarAlCarrito(pendiente, cantidad, getNombre(pendiente), usuario);

                productoPendienteMap.remove(usuario);

                return res + " 🛒 ¿Quieres algo más?";
            }

            return "Claro 😊 ¿Cuántos quieres de " + getNombre(pendiente) + "?";
        }

        // =========================
        // LISTA DE PRODUCTOS (NUEVO)
        // =========================
        if (msg.matches(".*\\b(ver productos|mostrar productos|lista de productos|que vendes|que tienes)\\b.*")) {

            StringBuilder res = new StringBuilder("🛒 Estos son algunos productos disponibles:\n\n");

            int count = 0;

            for (Producto p : productos) {

                res.append("• ")
                .append(getNombre(p))
                .append(" ($")
                .append(p.getPrecio())
                .append(")\n");

                count++;

                if (count >= 8) break; // no saturar
            }

            res.append("\nPuedes pedirme algo como:\n");
            res.append("- agrega 2 manzanas\n");
            res.append("- quiero 1 kilo de tomate");

            return res.toString();
        }

        // =========================
        // PRODUCTO INDIVIDUAL
        // =========================
        for (Producto p : productos) {

            if (coincideProducto(msg, p)) {

                double cantidad = detectarCantidadNumero(msg);

                if (cantidad > 0) {
                    return agregarAlCarrito(p, cantidad, getNombre(p), usuario)
                            + " 🛒 ¿Quieres algo más?";
                }

                productoPendienteMap.put(usuario, p);

                return "Claro 😊 tengo " + getNombre(p) + ", ¿cuántos deseas?";
            }
        }

        return "No estoy seguro de haber entendido 🤔\n\n"
            + "Puedo ayudarte con:\n"
            + "• Buscar productos (ej: 'ver productos')\n"
            + "• Agregar al carrito (ej: 'agrega 2 manzanas')\n"
            + "• Recetas (ej: 'receta con una berenjena')\n\n"
            + "¿Qué te gustaría hacer?";
    }

    private String normalizar(String texto) {

        if (texto == null) return "";

        texto = texto.toLowerCase().trim();

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        if (texto.endsWith("s")) {
            texto = texto.substring(0, texto.length() - 1);
        }

        return texto.replaceAll("\\s+", " ");
    }

    private String limpiarMensaje(String msg) {

        String[] stopWords = {
                "quiero", "dame", "agrega", "al", "carrito",
                "por favor", "vendeme", "me das", "ponme"
        };

        for (String w : stopWords) {
            msg = msg.replace(w, "").trim();
        }

        return msg;
    }

    private boolean coincideProducto(String msg, Producto p) {

        String nombre = normalizar(p.getNombre());
        String variedad = p.getVariedad() != null ? normalizar(p.getVariedad()) : "";

        if (!variedad.isEmpty() && msg.contains(nombre + " " + variedad)) return true;
        if (!variedad.isEmpty() && msg.contains(variedad)) return true;
        return msg.contains(nombre);
    }

    private Producto buscarMejorProducto(String msg, List<Producto> productos) {

        Producto mejor = null;
        int mejorScore = -1;

        for (Producto p : productos) {
            int score = scoreProducto(msg, p);

            if (score > mejorScore) {
                mejorScore = score;
                mejor = p;
            }
        }

        return mejor;
    }

    private int scoreProducto(String msg, Producto p) {

        String nombre = normalizar(p.getNombre());
        String variedad = p.getVariedad() != null ? normalizar(p.getVariedad()) : "";

        int score = 0;

        if (msg.contains(nombre + " " + variedad)) score += 5;
        if (!variedad.isEmpty() && msg.contains(variedad)) score += 3;
        if (msg.contains(nombre)) score += 1;

        return score;
    }

    private String getNombre(Producto p) {
        return capitalize(p.getNombre());
    }

    private String agregarAlCarrito(Producto p, double cantidad, String nombre, String usuario) {

        int cantidadEntera = (int) Math.ceil(cantidad);

        if (p.getStock() == null || p.getStock() < cantidadEntera) {

            if (p.getStock() != null && p.getStock() > 0) {
                carritoService.agregar(p, p.getStock(), usuario);
                return "Solo quedan " + p.getStock() + " de " + nombre + " 😮";
            }

            return "Ya no tenemos de ese producto, pero puedes seleccionar otro 😊";
        }

        carritoService.agregar(p, cantidadEntera, usuario);

        return "Perfecto 👍 agregué " + cantidadEntera + " de " + nombre + " al carrito";
    }

    private double detectarCantidadNumero(String msg) {

        msg = normalizar(msg);

        Map<String, Double> numeros = new HashMap<>();
        numeros.put("uno", 1.0);
        numeros.put("un", 1.0);
        numeros.put("dos", 2.0);
        numeros.put("tres", 3.0);
        numeros.put("cuatro", 4.0);
        numeros.put("cinco", 5.0);
        numeros.put("seis", 6.0);
        numeros.put("siete", 7.0);
        numeros.put("ocho", 8.0);
        numeros.put("nueve", 9.0);
        numeros.put("diez", 10.0);

        for (Map.Entry<String, Double> e : numeros.entrySet()) {
            if (msg.contains(e.getKey())) {
                return e.getValue();
            }
        }

        if (msg.contains("medio")) return 0.5;
        if (msg.contains("cuarto")) return 0.25;

        Pattern p = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(msg);

        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }

        return 0;
    }

    private String responderBienestar(String msg) {

        // No interferir con recetas ni con pedidos
        if (msg.contains("receta") ||
            msg.contains("agrega") ||
            msg.contains("anade") ||
            msg.contains("añade") ||
            msg.contains("carrito") ||
            msg.contains("tienes") ||
            msg.contains("hay") ||
            msg.contains("vendes")) {
            return null;
        }

        if (contieneAlguna(msg, "saludable", "sano", "comer mejor", "alimentacion balanceada", "alimentación balanceada", "bienestar alimentario", "alimentación saludable", "dieta saludable", "comida saludable", "vida saludable", "dame opciones saludables","dame una opción saludable")) {
            return "🥗 Recomendación de bienestar alimentario\n\n"
                + "Opciones sugeridas:\n"
                + "• Manzana\n"
                + "• Zanahoria\n"
                + "• Pepino\n"
                + "• Espinaca\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Son alimentos versátiles que pueden formar parte de una alimentación equilibrada.\n\n"
                + "Idea práctica:\n"
                + "Puedes combinarlos en ensaladas, bowls o colaciones frescas.\n\n"
                + "Siguiente paso:\n"
                + "Si quieres, puedo darte opciones para desayuno, fibra o vitamina C.";
        }

        if (contieneAlguna(msg, "ligero", "ligera", "liviano", "fresco", "fresca", "algo rápido", "algo fácil", "algo sencillo",  "recomiendame algo ligero", "quiero algo ligero", "dame opciones ligeras",  "recomiendame algo sencillo", "quiero algo sencillo", "dame opciones sencillas")) {
            return "🥒 Recomendación para una opción ligera\n\n"
                + "Opciones sugeridas:\n"
                + "• Pepino\n"
                + "• Lechuga\n"
                + "• Jitomate\n"
                + "• Manzana\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Son ingredientes frescos y fáciles de combinar en preparaciones ligeras.\n\n"
                + "Idea práctica:\n"
                + "Puedes preparar una ensalada fresca o una colación simple.\n\n"
                + "Siguiente paso:\n"
                + "También puedo darte opciones con fibra o para desayuno.";
        }

        if (contieneAlguna(msg, "desayuno", "desayunar", "mañana", "manana", "comenzar el día", "empezar el día", "algo para la mañana", "algo para comenzar el día", "algo para desayunar", "opciones para la mañana", "opciones para el desayuno", "recomiendame algo para desayunar")) {
            return "🍎 Recomendación para desayuno\n\n"
                + "Opciones sugeridas:\n"
                + "• Plátano\n"
                + "• Manzana\n"
                + "• Avena\n"
                + "• Fresa\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Son ingredientes prácticos para comenzar el día con una opción sencilla y equilibrada.\n\n"
                + "Idea práctica:\n"
                + "Avena con plátano y manzana en cubos, o fruta fresca con yogur.\n\n"
                + "Siguiente paso:\n"
                + "Si quieres, puedo darte una opción más ligera o con más fibra.";
        }

        if (contieneAlguna(msg, "fibra", "digestivo", "digestion", "digestión", "estreñimiento", "intestino", "regularidad", "fibroso", "alimentos con fibra", "alimentos ricos en fibra", "recomiendame alimentos altos en fibra", "quiero alimentos con fibra", "dame opciones con fibra")) {
            return "🌾 Recomendación con fibra\n\n"
                + "Opciones sugeridas:\n"
                + "• Manzana\n"
                + "• Pera\n"
                + "• Avena\n"
                + "• Zanahoria\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Estos alimentos suelen relacionarse con un mayor aporte de fibra dentro de una alimentación variada.\n\n"
                + "Idea práctica:\n"
                + "Puedes combinarlos en desayuno o como colación durante el día.\n\n"
                + "Siguiente paso:\n"
                + "También puedo recomendarte opciones saludables o para vitamina C.";
        }

        if (contieneAlguna(msg, "vitamina c", "vitamina c", "defensas", "inmunidad", "resistencia", "infecciones", "vitamina c alimentos", "alimentos con vitamina c", "alimentos ricos en vitamina c", "alimentos con vitamina c", "recomiendame alimentos con vitamina c", "quiero alimentos con vitamina c", "dame opciones con vitamina c")) {
            return "🍊 Recomendación con vitamina C\n\n"
                + "Opciones sugeridas:\n"
                + "• Naranja\n"
                + "• Fresa\n"
                + "• Kiwi\n"
                + "• Pimiento\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Son alimentos conocidos por aportar vitamina C dentro de una alimentación equilibrada.\n\n"
                + "Idea práctica:\n"
                + "Puedes usarlos en ensaladas frescas, jugos naturales o colaciones.\n\n"
                + "Siguiente paso:\n"
                + "Si quieres, también puedo darte opciones ligeras o para desayuno.";
        }

        if (contieneAlguna(msg, "energia", "energía", "cansancio", "activo", "energético", "energética", "alimentos para energía", "alimentos con energía", "alimentos energéticos", "recomiendame algo con energía", "quiero algo con energía", "dame opciones con energía")) {
            return "⚡ Recomendación para energía diaria\n\n"
                + "Opciones sugeridas:\n"
                + "• Plátano\n"
                + "• Avena\n"
                + "• Manzana\n"
                + "• Frutos secos\n\n"
                + "¿Por qué puede ayudarte?\n"
                + "Son opciones prácticas para colaciones o desayunos dentro de una rutina diaria.\n\n"
                + "Idea práctica:\n"
                + "Puedes combinarlos en un desayuno rápido o una colación de media mañana.\n\n"
                + "Siguiente paso:\n"
                + "También puedo darte una opción más ligera o con fibra.";
        }

        if (contieneAlguna(msg, "dieta", "dieta saludable", "comer mejor", "plan alimenticio", "alimentación saludable", "alimentacion saludable", "recomiendame una dieta", "quiero una dieta", "dame un plan alimenticio", "recomiendame un plan alimenticio", "quiero un plan alimenticio", "dame una dieta", "recomiendame una alimentación saludable", "quiero una alimentación saludable", "dame una alimentación saludable")) {
            return "💡 Asistente de bienestar alimentario\n\n"
                + "Puedo orientarte con recomendaciones generales como:\n"
                + "• Opciones saludables\n"
                + "• Ideas para desayuno\n"
                + "• Alimentos con fibra\n"
                + "• Opciones con vitamina C\n"
                + "• Alternativas ligeras\n\n"
                + "Importante:\n"
                + "Estas son sugerencias generales de alimentación y no sustituyen orientación médica o nutricional profesional.\n\n"
                + "Ejemplos de consulta:\n"
                + "• \"quiero algo saludable\"\n"
                + "• \"dame opciones con fibra\"\n"
                + "• \"recomiendame alimentos con vitamina c\"\n"
                + "• \"qué frutas tienen vitamina C\"";
        }

        return null;
    }

    private boolean contieneAlguna(String texto, String... opciones) {
        for (String opcion : opciones) {
            if (texto.contains(opcion)) {
                return true;
            }
        }
        return false;
    }

    private String capitalize(String str) {
        return (str == null || str.isEmpty())
                ? str
                : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}