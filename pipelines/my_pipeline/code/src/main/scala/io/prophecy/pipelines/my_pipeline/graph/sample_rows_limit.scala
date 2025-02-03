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

object sample_rows_limit {

  def apply(context: Context, in: DataFrame): DataFrame =
    in.sample(false, "0.1".toDouble, "50".toLong)

}
