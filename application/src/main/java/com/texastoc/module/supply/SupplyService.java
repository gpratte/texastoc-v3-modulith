package com.texastoc.module.supply;

import com.texastoc.module.supply.model.Supply;
import com.texastoc.module.supply.repository.SupplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class SupplyService {

  private final SupplyRepository supplyRepository;

  public SupplyService(SupplyRepository supplyRepository) {
    this.supplyRepository = supplyRepository;
  }

  public List<Supply> get() {
    return supplyRepository.get();
  }

  public Supply create(Supply supply) {
    Assert.notNull(supply.getDate(), "Date is required");
    Assert.isTrue(supply.getAmount() > 0, "Amount must be greater than zero");
    Assert.notNull(supply.getType(), "Type is required");
    supplyRepository.save(supply);
    return supply;
  }
}
