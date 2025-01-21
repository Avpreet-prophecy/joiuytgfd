package io.prophecy.pipelines.my_pipeline

import io.prophecy.libs._
import io.prophecy.pipelines.my_pipeline.config._
import io.prophecy.pipelines.my_pipeline.functions.UDFs._
import io.prophecy.pipelines.my_pipeline.functions.PipelineInitCode._
import io.prophecy.pipelines.my_pipeline.graph._
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions._
import java.time._

object Main {

  def apply(context: Context): Unit = {
    val df_Reformat_1 = Reformat_1(context)
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigurationFactoryImpl.getConfig(args)
    val spark: SparkSession = SparkSession
      .builder()
      .appName("my_pipeline")
      .enableHiveSupport()
      .getOrCreate()
    val context = Context(spark, config)
    spark.conf.set("prophecy.metadata.pipeline.uri",        "pipelines/my_pipeline")
    spark.conf.set("spark.default.parallelism",             "4")
    spark.conf.set("spark.sql.legacy.allowUntypedScalaUDF", "true")
    registerUDFs(spark)
    MetricsCollector.instrument(spark, "pipelines/my_pipeline") {
      apply(context)
    }
  }

}
