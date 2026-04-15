package com.tuapp.grocery_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

import com.tuapp.grocery_backend.model.Producto;
import com.tuapp.grocery_backend.repository.ProductoRepository;

@Service
public class CarritoService {

    private final ProductoRepository productoRepository;

    private final Map<String, List<Map<String, Object>>> carritos = new HashMap<>();

    public CarritoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    private List<Map<String, Object>> getCarrito(String usuario) {
        return carritos.computeIfAbsent(usuario, k -> new ArrayList<>());
    }

    // ✅ AGREGAR
    public String agregar(Producto producto, int cantidad, String usuario) {

        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        int stockActual = producto.getStock() == null ? 0 : producto.getStock();

        if (stockActual < cantidad) {
            throw new IllegalStateException("Stock insuficiente. Solo quedan " + stockActual + " unidades.");
        }

        List<Map<String, Object>> carrito = getCarrito(usuario);

        for (Map<String, Object> item : carrito) {
            Long id = ((Number) item.get("id")).longValue();
            String variedad = (String) item.get("variedad");

            if (id.equals(producto.getId()) &&
                variedad.equalsIgnoreCase(producto.getVariedad())) {

                int nuevaCantidad = ((Number) item.get("cantidad")).intValue() + cantidad;

                item.put("cantidad", nuevaCantidad);
                item.put("subtotal", producto.getPrecio() * nuevaCantidad);

                producto.setStock(stockActual - cantidad);
                productoRepository.save(producto);

                return "🛒 Producto agregado al carrito";
            }
        }

        Map<String, Object> item = new HashMap<>();
        item.put("id", producto.getId());
        item.put("nombre", producto.getNombre());
        item.put("variedad", producto.getVariedad());
        item.put("precio", producto.getPrecio());
        item.put("cantidad", cantidad);
        item.put("subtotal", producto.getPrecio() * cantidad);

        carrito.add(item);

        producto.setStock(stockActual - cantidad);
        productoRepository.save(producto);

        return "🛒 Producto agregado al carrito";
    }

    public List<Map<String, Object>> obtenerCarrito(String usuario) {
        return getCarrito(usuario);
    }

    public double obtenerTotal(String usuario) {
        return getCarrito(usuario).stream()
                .mapToDouble(item -> ((Number) item.get("subtotal")).doubleValue())
                .sum();
    }

    // ❌ ELIMINAR PRODUCTO Y DEVOLVER STOCK
    public void eliminarProducto(Long id, String variedad, String usuario) {

        List<Map<String, Object>> carrito = getCarrito(usuario);

        Iterator<Map<String, Object>> iterator = carrito.iterator();

        while (iterator.hasNext()) {
            Map<String, Object> item = iterator.next();

            Long itemId = ((Number) item.get("id")).longValue();
            String itemVariedad = (String) item.get("variedad");

            if (itemId.equals(id) && itemVariedad.equalsIgnoreCase(variedad)) {

                int cantidad = ((Number) item.get("cantidad")).intValue();

                productoRepository.findById(id).ifPresent(producto -> {
                    int stockActual = producto.getStock() == null ? 0 : producto.getStock();
                    producto.setStock(stockActual + cantidad);
                    productoRepository.save(producto);
                });

                iterator.remove();
                return;
            }
        }
    }

    // 🧹 VACIAR CARRITO Y DEVOLVER STOCK
    public void vaciarCarrito(String usuario) {

        List<Map<String, Object>> carrito = getCarrito(usuario);

        for (Map<String, Object> item : carrito) {
            Long id = ((Number) item.get("id")).longValue();
            int cantidad = ((Number) item.get("cantidad")).intValue();

            productoRepository.findById(id).ifPresent(producto -> {
                int stockActual = producto.getStock() == null ? 0 : producto.getStock();
                producto.setStock(stockActual + cantidad);
                productoRepository.save(producto);
            });
        }

        carrito.clear();
    }
}