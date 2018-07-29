package com.template.crp_clustering

import org.apache.predictionio.controller.IEngineFactory
import org.apache.predictionio.controller.Engine
import org.apache.spark.mllib.linalg.Vector

case class Query(
	point: Array[Double]
) extends Serializable

case class PredictedResult(val cluster: Array[Double]) extends Serializable

object CRPEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}