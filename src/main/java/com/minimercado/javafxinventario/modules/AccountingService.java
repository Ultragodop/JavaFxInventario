package com.minimercado.javafxinventario.modules;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class AccountingService {
    private AccountingModule accountingModule;

    public AccountingService() {
        accountingModule = new AccountingModule();
    }

    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/transactions", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if ("POST".equalsIgnoreCase(method)) {
                    InputStream is = exchange.getRequestBody();
                    String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                    String[] parts = body.split(",");
                    if(parts.length >= 3) {
                        String type = parts[0].trim();
                        double amount = Double.parseDouble(parts[1].trim());
                        String description = parts[2].trim();
                        Transaction tx = new Transaction(type, amount, description);
                        accountingModule.recordTransaction(tx);
                        String resp = "Transacción registrada";
                        exchange.sendResponseHeaders(200, resp.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(resp.getBytes());
                        os.close();
                    } else {
                        String resp = "Error: parámetros insuficientes";
                        exchange.sendResponseHeaders(400, resp.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(resp.getBytes());
                        os.close();
                    }
                } else if ("GET".equalsIgnoreCase(method)) {
                    StringBuilder sb = new StringBuilder();
                    for (Transaction tx : accountingModule.getTransactions()) {
                        sb.append("Tipo: ").append(tx.getType())
                          .append(", Monto: ").append(tx.getAmount())
                          .append(", Descripción: ").append(tx.getDescription())
                          .append("\n");
                    }
                    byte[] respBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, respBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(respBytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });
        server.createContext("/report", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String report = accountingModule.generateFinancialReport();
                    byte[] respBytes = report.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, respBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(respBytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("AccountingService iniciado en el puerto " + port);
    }

    public static void main(String[] args) {
        try {
            new AccountingService().startServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
