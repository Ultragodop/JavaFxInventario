package com.minimercado.javafxinventario.modules;

public class PaymentModule {

    public Payment processPayment(String paymentType, double saleAmount, double amountDelivered) {
        double change = 0.0;
        if ("efectivo".equalsIgnoreCase(paymentType)) {
            if (amountDelivered < saleAmount) {
                throw new IllegalArgumentException("El monto entregado es insuficiente.");
            }
            change = amountDelivered - saleAmount;
        } else if ("tarjeta".equalsIgnoreCase(paymentType)) {
            amountDelivered = saleAmount;
        } else {
            throw new IllegalArgumentException("Tipo de pago no soportado: " + paymentType);
        }
        Payment payment = new Payment(paymentType, saleAmount, amountDelivered, change);
        System.out.println("Pago procesado: " + payment);
        return payment;
    }

    public static class Payment {
        private String paymentType;
        private double saleAmount;
        private double amountDelivered;
        private double change;
        
        public Payment(String paymentType, double saleAmount, double amountDelivered, double change) {
            this.paymentType = paymentType;
            this.saleAmount = saleAmount;
            this.amountDelivered = amountDelivered;
            this.change = change;
        }
        public String getPaymentType() { return paymentType; }
        public double getSaleAmount() { return saleAmount; }
        public double getAmountDelivered() { return amountDelivered; }
        public double getChange() { return change; }
        @Override
        public String toString() {
            return "Payment{" +
                   "paymentType='" + paymentType + '\'' +
                   ", saleAmount=" + saleAmount +
                   ", amountDelivered=" + amountDelivered +
                   ", change=" + change +
                   '}';
        }
    }
}
