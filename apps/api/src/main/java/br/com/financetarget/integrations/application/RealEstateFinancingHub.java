package br.com.financetarget.integrations.application;

public interface RealEstateFinancingHub {
    record SimulationRequest(String propertyValue, String downPayment, String currency, int months) {}
    record IndicativeOffer(String reference, String financedAmount, String totalEffectiveCostAnnual,
                           int months, String amortizationSystem, String currency) {}

    IndicativeOffer simulate(SimulationRequest request);
}
