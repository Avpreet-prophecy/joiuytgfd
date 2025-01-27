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

object Aggregate_1 {

  case class AggregateProperties(
    activeTab:       String = "aggregate",
    columnsSelector: List[String] = Nil,
    groupBy:         List[(String, org.apache.spark.sql.Column)] = Nil,
    aggregate:       List[(String, org.apache.spark.sql.Column)] = Nil,
    doPivot:         Boolean = false,
    pivotColumn:     Option[org.apache.spark.sql.Column] = None,
    pivotValues:     List[StringColName] = Nil,
    allowSelection:  Option[Boolean] = Some(true),
    allIns:          Option[Boolean] = Some(false)
  )

  case class StringColName(colName: String)

  def apply(context: Context, in: DataFrame): DataFrame = {
    val spark  = context.spark
    val Config = context.config
    val props = AggregateProperties(
      activeTab = "aggregate",
      columnsSelector = List(),
      groupBy = List(),
      aggregate = List(),
      doPivot = false,
      pivotColumn = None,
      pivotValues = List(),
      allowSelection = Some(true),
      allIns = Some(false)
    )
    val propagate_cols = props.allIns match {
      case None =>
        props.aggregate.tail.map(x => x._2.as(x._1))
      case Some(value) =>
        if (value) {
          val agg_cols = props.aggregate.map(x => x._1)
          val groupBy_cols =
            if (props.doPivot)
              props.pivotColumn.get.toString() :: props.groupBy.map(x => x._1)
            else
              props.groupBy.map(x => x._1)
          props.aggregate.tail.map(x => x._2.as(x._1)) ++ in.columns.toList
            .diff(agg_cols ++ groupBy_cols)
            .map(x => first(col(x)).as(x))
        } else
          props.aggregate.tail.map(x => x._2.as(x._1))
    }
    val out = props.groupBy match {
      case Nil =>
        in.agg(props.aggregate.head._2.alias(props.aggregate.head._1),
               propagate_cols: _*
        )
      case _ =>
        val grouped = in.groupBy(props.groupBy.map(x => x._2.as(x._1)): _*)
        val pivoted = if (props.doPivot) {
          (props.pivotValues.map(_.colName), props.pivotColumn.get) match {
            case (Nil, column) =>
              grouped.pivot(column)
            case (nonNil, column) =>
              grouped.pivot(column, nonNil)
            case _ =>
              grouped
          }
        } else
          grouped
        pivoted.agg(props.aggregate.head._2.alias(props.aggregate.head._1),
                    propagate_cols: _*
        )
    }
    out
  }

}
