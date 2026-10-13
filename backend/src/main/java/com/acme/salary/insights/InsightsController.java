package com.acme.salary.insights;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.salary.insights.InsightsResponses.CountryDetail;
import com.acme.salary.insights.InsightsResponses.OrgOverview;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/countries")
    public OrgOverview overview() {
        return service.overview();
    }

    @GetMapping("/countries/{countryCode}")
    public CountryDetail countryDetail(@PathVariable String countryCode) {
        return service.countryDetail(countryCode);
    }
}
