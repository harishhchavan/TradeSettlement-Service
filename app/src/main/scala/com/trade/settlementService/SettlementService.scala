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
import java.util.concurrent.{Executors, TimeUnit}

object SettlementService {

  def main(args: Array[String]): Unit = {

    val dbConfig = DbConfig.dbConfig
    Class.forName(dbConfig.dbDriver)

    val ampsConfig = AmpsConfig.ampsConfig

    println("=" * 60)
    println("Welcome to")
    println("SETTLEMENT SERVICE by HARISH")
    println("=" * 60)

    val client = AmpsClientUtil.connect("SettlementService")

    // ---------------- Scheduler ----------------
    val scheduler = Executors.newSingleThreadScheduledExecutor()

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

              println("Connected to Shared Database...")

              try {
                conn.setAutoCommit(false)
                updateSettlementInDB(conn, settled)
                conn.commit()
                println(s"Trade ${settled.trade_id} settled successfully.")

              } catch {
                case e: Exception =>
                  conn.rollback()
                  e.printStackTrace()

              } finally {
                conn.close()
              }

            case JsError(errors) =>
              println("Invalid JSON received")
              println(errors)
          }
        }
      }

      val cmd = new Command("subscribe")
        .setTopic(ampsConfig.topicTradesFigurated)

      client.executeAsync(cmd, handler)

      println(s"Listening to AMPS topic: #${ampsConfig.topicTradesFigurated}")

      // -------- Scheduler task (keeps service alive) --------
      scheduler.scheduleAtFixedRate(
        () => println(s"[Heartbeat] SettlementService alive @ ${Instant.now()}"),
        0,
        60,
        TimeUnit.SECONDS
      )

    } finally {
      sys.addShutdownHook {
        println("Shutting down SettlementService...")
        scheduler.shutdown()
        client.close()
      }
    }
  }

  // ---------------- Settlement Logic ----------------

  private def settleTrade(t: Trade): Trade = {

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
    println("Updated Trade in Database")
  }
}
