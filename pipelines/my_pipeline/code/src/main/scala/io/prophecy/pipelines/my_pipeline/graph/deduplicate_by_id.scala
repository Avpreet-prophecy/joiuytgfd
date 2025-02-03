package io.prophecy.pipelines.my_pipeline.graph

import io.prophecy.libs._
import io.prophecy.pipelines.my_pipeline.functions.PipelineInitCode._
import io.prophecy.pipelines.my_pipeline.functions.UDFs._
import io.prophecy.pipelines.my_pipeline.config.Context
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions._
import java.time._

object deduplicate_by_id {

  def apply(context: Context, in: DataFrame): DataFrame = {
    import org.apache.spark.sql.expressions.Window
    in.withColumn("row_number",
                  row_number().over(Window.partitionBy("id").orderBy(lit(1)))
      )
      .withColumn("count", count("*").over(Window.partitionBy("id")))
      .filter(col("row_number") === col("count"))
      .drop("row_number")
      .drop("count")
  }

}
