package io.prophecy.pipelines.my_pipeline.graph

import io.prophecy.libs._
import io.prophecy.pipelines.my_pipeline.config.Context
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions._
import java.time._

object Mydataset {

  def apply(context: Context): DataFrame =
    context.spark.read
      .format("csv")
      .option("header", true)
      .option("sep",    ",")
      .schema(
        StructType(
          Array(StructField("id",   StringType, true),
                StructField("name", StringType, true)
          )
        )
      )
      .load("dbfs:/Prophecy/qa_data/csv/all_properties_data/data.csv")

}
