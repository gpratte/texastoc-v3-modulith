package com.texastoc.service;

import com.texastoc.TestConstants;
import com.texastoc.model.supply.Supply;
import com.texastoc.model.supply.SupplyType;
import com.texastoc.repository.SupplyRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RunWith(SpringRunner.class)
public class SupplyServiceTest implements TestConstants {

  private SupplyService supplyService;

  @MockBean
  private SupplyRepository supplyRepository;

  @Before
  public void before() {
    supplyService = new SupplyService(supplyRepository);
  }

  @Test
  public void testNoSupplies() {

    Mockito.when(supplyRepository.get()).thenReturn(Collections.emptyList());

    List<Supply> supplies = supplyService.get();

    Mockito.verify(supplyRepository, Mockito.times(1)).get();

    Assert.assertNotNull("supplies should not be null", supplies);
    Assert.assertEquals("number of supplies 0", 0, supplies.size());
  }

  @Test
  public void testSupplies() {

    List<Supply> currentSupplies = new LinkedList<>();
    currentSupplies.add(0, Supply.builder()
      .id(1)
      .build());
    currentSupplies.add(0, Supply.builder()
      .id(2)
      .build());
    currentSupplies.add(0, Supply.builder()
      .id(3)
      .build());
    Mockito.when(supplyRepository.get()).thenReturn(currentSupplies);

    List<Supply> supplies = supplyService.get();

    Mockito.verify(supplyRepository, Mockito.times(1)).get();

    Assert.assertNotNull("supplies should not be null", supplies);
    Assert.assertEquals("number of supplies 3", 3, supplies.size());

    Assert.assertEquals("first supply id 3", 3, supplies.get(0).getId());
    Assert.assertEquals("first supply id 2", 2, supplies.get(1).getId());
    Assert.assertEquals("first supply id 1", 1, supplies.get(2).getId());
  }

  @Test
  public void testCreateSupply() {
    supplyService.create(Supply.builder()
      .amount(10)
      .date(LocalDate.now())
      .type(SupplyType.CARDS)
      .description("new cards")
      .build());

    Mockito.verify(supplyRepository, Mockito.times(1)).save(Mockito.any(Supply.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingDateCreateSupply() {
    supplyService.create(Supply.builder()
      .amount(10)
      .type(SupplyType.CARDS)
      .build());

  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAmountCreateSupply() {
    supplyService.create(Supply.builder()
      .date(LocalDate.now())
      .amount(0)
      .type(SupplyType.CARDS)
      .build());

  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTypeCreateSupply() {
    supplyService.create(Supply.builder()
      .date(LocalDate.now())
      .amount(10)
      .build());
  }

}
