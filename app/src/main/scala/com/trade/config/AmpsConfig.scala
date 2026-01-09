package com.trade.config

case class AmpsConfig(
                       host: String,
                       port: Int,
                       topicTradesRaw: String,
                       topicTradesEnriched: String,
                       topicTradesValidated: String,
                       topicTradesFigurated: String,
                       topicTradesSettled: String
                     )

object AmpsConfig extends AbstractConfig {
  val ampsConfig: AmpsConfig = AmpsConfig(
    host = config.getString("amps.host"),
    port = config.getInt("amps.port"),
    topicTradesRaw = config.getString("amps.topicTradesRaw"),
    topicTradesEnriched = config.getString("amps.topicTradesEnriched"),
    topicTradesValidated = config.getString("amps.topicTradesValidated"),
    topicTradesFigurated = config.getString("amps.topicTradesFigurated"),
    topicTradesSettled = config.getString("amps.topicTradesSettled")
  )
}
