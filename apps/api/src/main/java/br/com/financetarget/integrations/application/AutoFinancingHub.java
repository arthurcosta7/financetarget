package br.com.financetarget.integrations.application;

public interface AutoFinancingHub {
    record SimulationRequest(String vehicleValue, String downPayment, String currency, int months) {}
    record IndicativeOffer(String reference, String financedAmount, String totalEffectiveCostAnnual,
                           int months, String currency) {}

    IndicativeOffer simulate(SimulationRequest request);
}
