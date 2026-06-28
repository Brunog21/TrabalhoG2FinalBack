package br.edu.atitus.currencyservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currencyservice.dtos.CurrencyDTO;
import br.edu.atitus.currencyservice.services.CurrencyConversionService;
import br.edu.atitus.currencyservice.services.CurrencyConversionService.ConversionResult;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyConversionService currencyConversionService;

    public CurrencyController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/convert")
    public ResponseEntity<CurrencyDTO> getCurrency(
            @RequestParam("source") String source,
            @RequestParam("target") String target) {
        ConversionResult result = currencyConversionService.convert(source, target);

        CurrencyDTO dto = new CurrencyDTO(
                result.sourceCurrency(),
                result.targetCurrency(),
                result.conversionRate(),
                result.environment());

        return ResponseEntity.ok(dto);
    }
}
