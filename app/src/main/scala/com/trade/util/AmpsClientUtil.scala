package com.trade.util

import com.crankuptheamps.client.{DefaultServerChooser, HAClient}
import com.trade.config.AmpsConfig

object AmpsClientUtil {

  def connect(clientName: String): HAClient = {
    val amps = AmpsConfig.ampsConfig
    val serverUrl = s"tcp://${amps.host}:${amps.port}/amps/json"

    val client = new HAClient(clientName)
    val chooser = new DefaultServerChooser()
    chooser.add(serverUrl)
    client.setServerChooser(chooser)

    client.connectAndLogon()
    client
  }
}
