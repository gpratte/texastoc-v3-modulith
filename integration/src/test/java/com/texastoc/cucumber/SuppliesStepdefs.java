package com.texastoc.cucumber;

import com.texastoc.model.supply.Supply;
import com.texastoc.model.supply.SupplyType;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class SuppliesStepdefs extends SpringBootBaseIntegrationTest {

  Supply supplyToCreate;
  List<Supply> suppliesRetrieved;
  Exception exception;

  @Before
  public void before() {
    supplyToCreate = null;
    suppliesRetrieved = null;
    exception = null;
  }

  @Given("^chairs have been bought$")
  public void chairs_have_been_bought() throws Exception {
    supplyToCreate = Supply.builder()
      .amount(11)
      .type(SupplyType.CHAIRS)
      .date(LocalDate.now())
      .build();
  }

  @When("^the supply is created$")
  public void the_supply_is_created() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    createSupply(supplyToCreate, token);
  }

  @When("^the supply is created by non admin$")
  public void the_supply_is_created_by_non_admin() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    try {
      createSupply(supplyToCreate, token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @Then("^the supplies are retrieved$")
  public void the_supplies_are_retrieved() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    suppliesRetrieved = getSupplies(token);
  }

  @Then("^then supply is in the list$")
  public void then_supply_is_in_the_list() throws Exception {
    Assert.assertNotNull("supply list not null", suppliesRetrieved);
    Assert.assertEquals("supply list 1", 1, suppliesRetrieved.size());

    Supply supplyRetrieved = suppliesRetrieved.get(0);
    Assert.assertEquals("amount", supplyToCreate.getAmount(), supplyRetrieved.getAmount());
    Assert.assertEquals("date", supplyToCreate.getDate(), supplyRetrieved.getDate());
    Assert.assertEquals("type", supplyToCreate.getType(), supplyRetrieved.getType());
    Assert.assertNull("description null", supplyRetrieved.getDescription());
  }

  @Then("^the reply is unauthorized$")
  public void the_reply_is_unauthorized() throws Exception {
    Assert.assertNotNull("exception not null", exception);
    Assert.assertTrue("should be HttpClientErrorException", exception instanceof HttpClientErrorException);

    HttpClientErrorException httpException = (HttpClientErrorException) exception;
    Assert.assertEquals("should be ", HttpStatus.FORBIDDEN, httpException.getStatusCode());
  }

}
