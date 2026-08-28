package br.com.financetarget.system.infrastructure.web;

import br.com.financetarget.config.FeatureFlagProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureFlagController {
    private final FeatureFlagProperties features;

    public FeatureFlagController(FeatureFlagProperties features) { this.features = features; }

    @GetMapping
    ResponseEntity<FeatureResponse> current() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new FeatureResponse(
                features.paymentsMock(), features.notificationsMock(), features.openFinance(), features.loyalty(),
                features.travel(), features.realEstateFinancing(), features.autoFinancing()));
    }

    public record FeatureResponse(boolean paymentsMock, boolean notificationsMock, boolean openFinance,
                                  boolean loyalty, boolean travel, boolean realEstateFinancing,
                                  boolean autoFinancing) {}
}
