package com.oceanview.service;

import java.util.HashMap;
import java.util.Map;

public class BillingService {
    // Standard rates for the resort
    private static final double TAX_RATE = 0.08;       // 8% Gov Tax
    private static final double SERVICE_CHARGE = 0.10; // 10% Service Charge

    /**
     * Performs financial calculations for the invoice.
     */
    public Map<String, Double> calculateInvoice(double roomTotal) {
        Map<String, Double> bill = new HashMap<>();
        
        double tax = roomTotal * TAX_RATE;
        double service = roomTotal * SERVICE_CHARGE;
        double grandTotal = roomTotal + tax + service;

        bill.put("subtotal", roomTotal);
        bill.put("tax", tax);
        bill.put("serviceCharge", service);
        bill.put("total", grandTotal);

        return bill;
    }
}