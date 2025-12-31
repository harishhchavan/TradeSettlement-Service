package com.trade.settlementService

import com.crankuptheamps.client.{Command, Message, MessageHandler}
import com.trade.SQL.SQL
import com.trade.model.Trade
import com.trade.config.DbConfig
import com.trade.util.AmpsClientUtil
import com.trade.config.AmpsConfig
import play.api.libs.json._

import java.sql.{Connection, DriverManager}
import java.time.Instant

object SettlementService {

  def main(args: Array[String]): Unit = {

    val dbConfig = DbConfig.dbConfig
    Class.forName(dbConfig.dbDriver)

    val ampsConfig = AmpsConfig.ampsConfig

    println("=" * 60)
    println("HARISH - SETTLEMENT SERVICE")
    println("=" * 60)

    val client = AmpsClientUtil.connect("SettlementService")

    try {
      println("Connected to AMPS Server")

      val handler = new MessageHandler {
        override def invoke(msg: Message): Unit = {

          val rawJson = msg.getData

          // Defensive cleanup (test env issue)
          val cleanJson =
            if (rawJson.contains("}"))
              rawJson.substring(0, rawJson.indexOf("}") + 1)
            else rawJson

          Json.parse(cleanJson).validate[Trade] match {

            case JsSuccess(trade, _) =>

              val settled = settleTrade(trade)

              val conn: Connection =
                DriverManager.getConnection(
                  dbConfig.dbUrl,
                  dbConfig.dbUser,
                  dbConfig.dbPassword
                )

              try {
                conn.setAutoCommit(false)
                updateSettlementInDB(conn, settled)
                conn.commit()
                println(s"Trade ${settled.trade_id} settled successful.")

              } catch {
                case e: Exception =>
                  conn.rollback()
                  e.printStackTrace()

              } finally {
                conn.close()
              }

            case JsError(errors) =>
              println("Invalid JSON")
              println(errors)
          }
        }
      }

      val cmd = new Command("subscribe")
        .setTopic(ampsConfig.topicTradesFigured)

      client.executeAsync(cmd, handler)

      println("Listening to AMPS topic: " + ampsConfig.topicTradesFigured)
      Thread.sleep(300000)

    } finally {
      client.close()
    }
  }

  // ---------------- Settlement Logic ----------------

  private def settleTrade(t: Trade): Trade = {

    val commission = t.quantity * t.price * 0.003
    val tax        = t.quantity * t.price * 0.005
    val gross      = t.quantity * t.price
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

  private def updateSettlementInDB(conn: Connection, t: Trade): Unit = {

    val ps = conn.prepareStatement(SQL.SETTLE_QUERY)

    ps.setBigDecimal(1, t.price.bigDecimal)
    ps.setString(2, t.broker_id)
    ps.setBigDecimal(3, t.commission.bigDecimal)
    ps.setBigDecimal(4, t.tax.bigDecimal)
    ps.setBigDecimal(5, t.gross_amount.bigDecimal)
    ps.setBigDecimal(6, t.net_amount.bigDecimal)
    ps.setString(7, t.received_time)
    ps.setString(8, t.status)
    ps.setInt(9, t.trade_id)

    ps.executeUpdate()
    ps.close()
  }
}

