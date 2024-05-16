package com.joshi.weatherinflux.common;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;

public class CDCSources {
  public static final TableDescriptor CPU_UTIL_DETAILS =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("id", DataTypes.STRING().notNull())
                  .column("device_id", DataTypes.STRING().notNull())
                  .column("nms_device_name", DataTypes.STRING().notNull())
                  .column("nms_instance_description", DataTypes.STRING().notNull())
                  .column("nms_ds0_description", DataTypes.STRING().notNull())
                  // ds1 is not needed for cpu util
                  // .column("nms_ds1_description", DataTypes.STRING().nullable())
                  .column("inv_acna", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("id")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "cpu_util_cdc_details")
          .option("heartbeat.interval", "1s")
          .build();

  public static final TableDescriptor TOTAL_BYTES_CDC =
      TableDescriptor.forConnector("mysql-cdc")
          .schema(
              Schema.newBuilder()
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("id", DataTypes.STRING().notNull())
                  .column("intf_id", DataTypes.STRING().notNull())
                  .column("device_id", DataTypes.STRING().notNull())
                  .column("nms_device_name", DataTypes.STRING().notNull())
                  .column("nms_instance_description", DataTypes.STRING().notNull())
                  .column("nms_ds0_description", DataTypes.STRING().notNull())
                  .column("nms_ds1_description", DataTypes.STRING().nullable())
                  .column("inv_acna", DataTypes.STRING().notNull())
                  .columnByExpression("t_proctime", "PROCTIME()")
                  .primaryKey("nms_region", "device_id", "intf_id")
                  .build())
          .option("hostname", "mm-mariadb-for-auto-metrics")
          .option("port", "3306")
          .option("username", "boss")
          .option("password", "IMBOSS")
          .option("database-name", "Netreo")
          .option("table-name", "intf_total_bytes_cdc_details")
          .option("heartbeat.interval", "1s")
          .build();
}
