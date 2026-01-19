package com.trade.model

import play.api.libs.json.{Format, Json}

case class Trade(
                  trade_id: Int,
                  order_id: String,
                  execution_id: String,
                  symbol: String,
                  side: String,
                  quantity: BigDecimal,
                  price: BigDecimal,
                  trade_time: String,
                  venue: String,
                  currency: String,
                  account_id: String,
                  broker_id: String,
                  commission: BigDecimal,
                  tax: BigDecimal,
                  gross_amount: BigDecimal,
                  net_amount: BigDecimal,
                  received_time: String,
                  status: String
                )

object Trade {

  implicit val tradeFormat: Format[Trade] = Json.format[Trade]

  def empty(): Trade = Trade(
    trade_id = 0,
    order_id = "",
    execution_id = "",
    symbol = "",
    side = "",
    quantity = BigDecimal(0),
    price = BigDecimal(0),
    trade_time = "",
    venue = "",
    currency = "",
    account_id = "",
    broker_id = "",
    commission = BigDecimal(0),
    tax = BigDecimal(0),
    gross_amount = BigDecimal(0),
    net_amount = BigDecimal(0),
    received_time = "",
    status = ""
  )
}
