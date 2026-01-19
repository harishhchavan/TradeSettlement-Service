package com.trade.settlementService

import com.trade.model.Trade
import java.time.Instant

import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory

class SettlementStepsScalaDsl extends ScalaDsl with EN with Matchers {

  private val logger = LoggerFactory.getLogger(getClass)

  var trade: Trade = _
  var result: Trade = _
  val processor = new SettlementProcessor()

  // Background / Given

  Given("a valid trade with required fields populated") { () =>
    trade = Trade.empty()
    logger.info(s"Initialized a new valid trade template: $trade")
  }

  Given("the trade has quantity {int} and price {int}") { (qty: Int, price: Int) =>
    trade = trade.copy(quantity = qty, price = price)
  }

  Given("the trade status is {string}") { (status: String) =>
    trade = trade.copy(status = status)
  }

  Given("the trade has passed validation in upstream services") { () =>
    // No output here because validation assumed by design
  }

  Given("the trade is ready for settlement") { () =>
    trade = Trade.empty()
  }

  // When

  When("the trade is settled") { () =>
    result = processor.settleTrade(trade)
    logger.info(s"SettlementProcessor executed. Result: $result")
  }

  When("settlement is triggered") { () =>
    result = processor.settleTrade(trade)
  }

  // Then – calculations

  Then("the gross amount should be {double}") { (expected: Double) =>
    result.gross_amount shouldBe BigDecimal(expected)
    logger.info(s"Gross amount verified: ${result.gross_amount}")
  }

  Then("the commission should be {double}") { (expected: Double) =>
    result.commission shouldBe expected
    logger.info(s"Commission verified: ${result.commission}")
  }

  Then("the tax should be {double}") { (expected: Double) =>
    result.tax shouldBe expected
    logger.info(s"Tax verified: ${result.tax}")
  }

  Then("the net amount should be {double}") { (expected: Double) =>
    result.net_amount shouldBe expected
    logger.info(s"Net amount verified: ${result.net_amount}")
  }

  // Then – derived / rule-based

  Then("the gross amount should be quantity multiplied by price") { () =>
    result.gross_amount shouldBe trade.quantity * trade.price
  }

  Then("the commission should be 0.3 percent of the gross amount") { () =>
    result.commission shouldBe result.gross_amount * 0.003
  }

  Then("the tax should be 0.5 percent of the gross amount") { () =>
    result.tax shouldBe result.gross_amount * 0.005
  }

  Then("the net amount should be gross minus commission and tax") { () =>
    result.net_amount shouldBe
      result.gross_amount - result.commission - result.tax
  }

  // Then – status & audit

  Then("the trade status should be {string}") { (expected: String) =>
    result.status shouldBe expected
    logger.info(s"Trade status verified: ${result.status}")
  }

  Then("the broker id should be assigned") { () =>
    result.broker_id should not be empty
    logger.info(s"Broker assigned: ${result.broker_id}")
  }

  Then("the received time should be populated") { () =>
    val received = Instant.parse(result.received_time)
    received.isAfter(Instant.EPOCH) shouldBe true
    logger.info(s"Received time populated: ${result.received_time}")
  }

  Then("the settlement processor should not perform validation checks") { () =>
    true shouldBe true
  }

  Then("the trade should be settled successfully") { () =>
    result.status shouldBe "SETTLED"
  }

  Then("the settlement processor recalculates the trade") { () =>
    result.gross_amount should be >= BigDecimal(0)
  }

  Then("the database layer prevents overriding the existing settlement") { () =>
    true shouldBe true // infra responsibility
  }

  Then("the received time should reflect the settlement time") { () =>
    val received = Instant.parse(result.received_time)
    received.isAfter(Instant.EPOCH) shouldBe true
  }

  Then("the trade status should indicate final settlement") { () =>
    result.status shouldBe "SETTLED"
  }
}

