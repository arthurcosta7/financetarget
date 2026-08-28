package br.com.financetarget.integrations.application;

import java.time.Instant;
import java.time.LocalDate;

public interface TravelHub {
    record EstimateRequest(String origin, String destination, LocalDate departure, LocalDate returnDate,
                           String currency) {}
    record Estimate(String reference, String amount, String currency, Instant observedAt) {}

    Estimate estimate(EstimateRequest request);
}
