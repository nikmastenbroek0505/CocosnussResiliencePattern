package com.ms.cocosnussResiliencePattern.controller;

import com.ms.cocosnussResiliencePattern.models.Order;
import com.ms.cocosnussResiliencePattern.repo.OrderRepo;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//------------------------------------ Abgeschlossen ------------------------------------//
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepo orderRepo;

    // Gibt alle Bestellungen zurück
    @Retry(name = "getAllOrdersRetry", fallbackMethod = "fallbackGetAllOrders")
    @TimeLimiter(name = "getAllOrdersTimeout")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Order>>> getAllOrders(
            @RequestParam(required = false) String errorType
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if ("retry".equalsIgnoreCase(errorType)) {
                    System.out.println("💥 Simuliere Retry-Fehler...");
                    throw new RuntimeException("Künstlicher Fehler für Retry-Test");
                }

                if ("timeout".equalsIgnoreCase(errorType)) {
                    System.out.println("🕒 Simuliere Timeout...");
                    Thread.sleep(3000); // länger als Timeout-Konfig
                }

                List<Order> orders = orderRepo.findAll();
                if (orders.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity<>(orders, HttpStatus.OK);

            } catch (InterruptedException e) {
                throw new RuntimeException("Timeout-Simulation unterbrochen", e);
            }
        });
    }

    public CompletableFuture<ResponseEntity<List<Order>>> fallbackGetAllOrders(String errorType, Throwable ex) {
        System.out.println("⚠️ Fallback aktiviert (" + errorType + "): " + ex.getClass().getSimpleName() + " – " + ex.getMessage());
        return CompletableFuture.completedFuture(
                new ResponseEntity<>(List.of(), HttpStatus.SERVICE_UNAVAILABLE)
        );
    }



    // Gibt eine bestimmte Bestellung nach ID zurück
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderRepo.findById(id);
        return order.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Neue Bestellung anlegen
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order newOrder) {
        try {
            Order saved = orderRepo.save(newOrder);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Bestehende Bestellung patchen (teilweise aktualisieren)
    @PatchMapping("/{id}")
    public ResponseEntity<Order> patchOrder(@PathVariable Long id, @RequestBody Order patchData) {
        Optional<Order> optionalOrder = orderRepo.findById(id);
        if (optionalOrder.isPresent()) {
            Order existing = optionalOrder.get();

            // Nur die übergebenen Felder patchen
            if (patchData.getStatus() != null) {
                existing.setStatus(patchData.getStatus());
            }
            if (patchData.getShippingAdress() != null) {
                existing.setShippingAdress(patchData.getShippingAdress());
            }
            if (patchData.getUser() != null) {
                existing.setUser(patchData.getUser());
            }
            if (patchData.getProducts() != null && !patchData.getProducts().isEmpty()) {
                existing.setProducts(patchData.getProducts());
            }
            if (patchData.getCreatedAt() != null) {
                existing.setCreatedAt(patchData.getCreatedAt());
            }

            return new ResponseEntity<>(orderRepo.save(existing), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
