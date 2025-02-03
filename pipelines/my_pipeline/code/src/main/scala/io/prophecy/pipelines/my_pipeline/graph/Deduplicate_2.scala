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

object Deduplicate_2 {

  case class DeduplicateProperties(
    columnsSelector: List[String] = Nil,
    dedupType:       String = "any",
    dedupColumns:    List[StringColName] = Nil,
    useOrderBy:      Option[Boolean] = Some(false),
    orders:          Option[List[OrderByRule]] = Some(Nil)
  )

  case class StringColName(colName: String)

  case class OrderByRule(
    expression: org.apache.spark.sql.Column,
    sortType:   String
  )

  def apply(context: Context, in: DataFrame): DataFrame = {
    val spark  = context.spark
    val Config = context.config
    val props = DeduplicateProperties(columnsSelector = List(),
                                      dedupType = "any",
                                      dedupColumns = List(),
                                      useOrderBy = Some(false),
                                      orders = Some(List())
    )
    val df =
      if (props.dedupType == "distinct")
        in.distinct()
      else {
        import org.apache.spark.sql.expressions.Window
        val typeToKeep     = props.dedupType
        val groupByColumns = props.dedupColumns.map(_.colName)
        val orderRules = if (props.useOrderBy.contains(true)) {
          props.orders.get.map(x =>
            x.sortType match {
              case "asc" =>
                x.expression.asc
              case _ =>
                x.expression.desc
            }
          )
        } else
          List(lit(1))
        val window = Window
          .partitionBy(groupByColumns.head, groupByColumns.tail: _*)
          .orderBy(orderRules: _*)
        val windowForCount =
          Window.partitionBy(groupByColumns.head, groupByColumns.tail: _*)
        typeToKeep match {
          case "any" =>
            val columns =
              if (groupByColumns.isEmpty) in.columns.toList else groupByColumns
            in.dropDuplicates(columns)
          case "first" =>
            val dataFrameWithRowNumber =
              in.withColumn("row_number", row_number().over(window))
            dataFrameWithRowNumber
              .filter(col("row_number") === lit(1))
              .drop("row_number")
          case "last" =>
            val dataFrameWithRowNumber = in
              .withColumn("row_number", row_number().over(window))
              .withColumn("count",      count("*").over(windowForCount))
            dataFrameWithRowNumber
              .filter(col("row_number") === col("count"))
              .drop("row_number")
              .drop("count")
          case "unique_only" =>
            val dataFrameWithCount =
              in.withColumn("count", count("*").over(windowForCount))
            dataFrameWithCount.filter(col("count") === lit(1)).drop("count")
        }
      }
    df
  }

}
