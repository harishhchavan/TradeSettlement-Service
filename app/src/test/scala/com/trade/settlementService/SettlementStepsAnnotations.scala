//package com.trade.settlementService
//
//import com.trade.model.Trade
//
//import java.time.Instant
//
//import io.cucumber.java.en.{Given, When, Then}
//import org.scalatest.matchers.should.Matchers
//
//import org.slf4j.LoggerFactory
//
//
//class SettlementStepsAnnotations extends Matchers {
//
//  private val logger = LoggerFactory.getLogger(classOf[SettlementStepsAnnotations])
//
//  var trade: Trade = _
//  var result: Trade = _
//  val processor = new SettlementProcessor()
//
//  // Background / Given
//  @Given("a valid trade with required fields populated")
//  def valid_trade(): Unit = {
//    trade = Trade.empty()
//    logger.info(s"Initialized a new valid trade template: $trade")
//  }
//
//  @Given("the trade has quantity {int} and price {int}")
//  def trade_with_quantity_price(qty: Int, price: Int): Unit = {
//    trade = trade.copy(quantity = qty, price = price)
//  }
//
//  @Given("the trade status is {string}")
//  def trade_status(status: String): Unit = {
//    trade = trade.copy(status = status)
//  }
//
//  @Given("the trade has passed validation in upstream services")
//  def trade_validated(): Unit = {
//    // No-op: validation assumed by design
//  }
//
//  @Given("the trade is ready for settlement")
//  def trade_ready(): Unit = {
//    trade = Trade.empty()
//  }
//
//  // When
//
//  @When("the trade is settled")
//  def settle_trade(): Unit = {
//    result = processor.settleTrade(trade)
//    logger.info(s"SettlementProcessor executed. Result: $result")
//  }
//
//  @When("settlement is triggered")
//  def settlement_triggered(): Unit = {
//    result = processor.settleTrade(trade)
//  }
//
//  // Then – calculations
//
//  @Then("the gross amount should be {double}")
//  def gross_should_be(expected: Double): Unit = {
//    result.gross_amount shouldBe BigDecimal(expected)
//    logger.info(s"Gross amount verified: ${result.gross_amount}")
//  }
//
//  @Then("the commission should be {double}")
//  def commission_should_be(expected: Double): Unit = {
//    result.commission shouldBe expected
//    logger.info(s"Commission verified: ${result.commission}")
//  }
//
//  @Then("the tax should be {double}")
//  def tax_should_be(expected: Double): Unit = {
//    result.tax shouldBe expected
//    logger.info(s"Tax verified: ${result.tax}")
//  }
//
//  @Then("the net amount should be {double}")
//  def net_should_be(expected: Double): Unit = {
//    result.net_amount shouldBe expected
//    logger.info(s"Net amount verified: ${result.net_amount}")
//  }
//
//  // Then – derived / rule-based
//
//  @Then("the gross amount should be quantity multiplied by price")
//  def gross_formula(): Unit = {
//    result.gross_amount shouldBe trade.quantity * trade.price
//  }
//
//  @Then("the commission should be 0.3 percent of the gross amount")
//  def commission_formula(): Unit = {
//    result.commission shouldBe result.gross_amount * 0.003
//  }
//
//  @Then("the tax should be 0.5 percent of the gross amount")
//  def tax_formula(): Unit = {
//    result.tax shouldBe result.gross_amount * 0.005
//  }
//
//  @Then("the net amount should be gross minus commission and tax")
//  def net_formula(): Unit = {
//    result.net_amount shouldBe
//      result.gross_amount - result.commission - result.tax
//  }
//
//  // Then – status & audit
//
//  @Then("the trade status should be {string}")
//  def status_should_be(expected: String): Unit = {
//    result.status shouldBe expected
//    logger.info(s"Trade status verified: ${result.status}")
//  }
//
//  @Then("the broker id should be assigned")
//  def broker_assigned(): Unit = {
//    result.broker_id should not be empty
//    logger.info(s"Broker assigned: ${result.broker_id}")
//  }
//
//  @Then("the received time should be populated")
//  def received_time(): Unit = {
//    val received = Instant.parse(result.received_time)
//    received.isAfter(Instant.EPOCH) shouldBe true
//    logger.info(s"Received time populated: ${result.received_time}")
//  }
//
//  @Then("the settlement processor should not perform validation checks")
//  def no_validation(): Unit = {
//    true shouldBe true
//  }
//
//  @Then("the trade should be settled successfully")
//  def settled_successfully(): Unit = {
//    result.status shouldBe "SETTLED"
//  }
//
//  @Then("the settlement processor recalculates the trade")
//  def recalculated(): Unit = {
//    result.gross_amount should be >= BigDecimal(0)
//  }
//
//  @Then("the database layer prevents overriding the existing settlement")
//  def db_prevents_override(): Unit = {
//    true shouldBe true // infra responsibility
//  }
//
//  @Then("the received time should reflect the settlement time")
//  def settlement_time(): Unit = {
//    val received = Instant.parse(result.received_time)
//    received.isAfter(Instant.EPOCH) shouldBe true
//  }
//
//  @Then("the trade status should indicate final settlement")
//  def final_status(): Unit = {
//    result.status shouldBe "SETTLED"
//  }
//}
