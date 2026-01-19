package com.trade.config

case class DbConfig(dbUrl: String, dbUser: String, dbPassword: String, dbDriver: String)

object DbConfig extends AbstractConfig {

  val dbConfig: DbConfig = DbConfig(
    dbUrl = config.getString("db.dbUrl"),
    dbUser = config.getString("db.dbUser"),
    dbPassword = config.getString("db.dbPassword"),
    dbDriver = config.getString("db.dbDriver")
  )
}
