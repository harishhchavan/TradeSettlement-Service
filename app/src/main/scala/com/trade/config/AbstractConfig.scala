package com.trade.config

import com.typesafe.config.{Config, ConfigFactory}

trait AbstractConfig {
  protected val config: Config = ConfigFactory.load()
}
