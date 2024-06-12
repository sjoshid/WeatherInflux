package com.joshi.weatherinflux.common;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;

public class CDCSources {
  public static final TableDescriptor DEVICE_CDC_DETAILS =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("id", DataTypes.STRING().notNull())
                  .column("inv_country", DataTypes.STRING().notNull())
                  .column("inv_acna", DataTypes.STRING().notNull())
                  .column("inv_sponsored_by", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("id")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "device")
          .option("heartbeat.interval", "1s")
          .build();

  public static final TableDescriptor INTERFACE_CDC_DETAILS =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("id", DataTypes.STRING().notNull())
                  .column("device_id", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("id")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "interface")
          .option("heartbeat.interval", "1s")
          .build();
}
