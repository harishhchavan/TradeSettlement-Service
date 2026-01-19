package com.trade.settlementService

import com.trade.model.Trade

import java.time.Instant

class SettlementProcessor {

   def settleTrade(t: Trade): Trade = {

    val gross      = t.quantity * t.price
    val commission = gross * 0.003
    val tax        = gross * 0.005
    val net        = gross - commission - tax

    t.copy(
      broker_id     = "BRK-101",
      commission    = commission,
      tax           = tax,
      gross_amount  = gross,
      net_amount    = net,
      received_time = Instant.now().toString,
      status        = "SETTLED"
    )
  }
}
