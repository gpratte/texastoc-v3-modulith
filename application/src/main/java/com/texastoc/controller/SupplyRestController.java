package com.texastoc.controller;

import com.texastoc.model.supply.Supply;
import com.texastoc.service.SupplyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("unused")
@RestController
public class SupplyRestController {

  private final SupplyService supplyService;

  public SupplyRestController(SupplyService supplyService) {
    this.supplyService = supplyService;
  }

  @GetMapping("/api/v2/supplies")
  public List<Supply> getSupplies() {
    return supplyService.get();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/api/v2/supplies")
  public Supply createSupply(@RequestBody Supply supply) {
    return supplyService.create(supply);
  }

}
