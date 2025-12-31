package com.trade.SQL

object SQL {
  val SETTLE_QUERY = "UPDATE trades SET price = ?, broker_id = ?, commission = ?, tax = ?, gross_amount = ?, net_amount = ?, received_time = ?, status = ? WHERE trade_id = ? AND status <> 'SETTLED'"
}