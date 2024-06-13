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
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("nms_device_id", DataTypes.INT().notNull())
                  .column("inv_device_name", DataTypes.STRING().notNull())
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
                  .column("nms_region", DataTypes.SMALLINT().notNull())
                  .column("device_id", DataTypes.STRING().notNull())
                  .column("nms_description", DataTypes.STRING().notNull())
                  .column("nms_interface_id", DataTypes.INT().notNull())
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

  public static final TableDescriptor CPU_UTIL_DETAILS_is_not_needed =
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

  public static final TableDescriptor TOTAL_BYTES_CDC_is_not_needed =
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
