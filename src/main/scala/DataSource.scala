package com.template.crp_clustering

import org.apache.predictionio.controller.PDataSource
import org.apache.predictionio.controller.EmptyEvaluationInfo
import org.apache.predictionio.controller.EmptyActualResult
import org.apache.predictionio.controller.Params
import org.apache.predictionio.data.storage.Event
import org.apache.predictionio.data.storage.PEvents
import org.apache.predictionio.data.storage.Storage
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(appName: String, appId: Int, dimention: Int) extends Params

class DataSource(val dsp: DataSourceParams) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  @transient lazy val logger = Logger[this.type]

	override
	def readTraining(sc: SparkContext): TrainingData = {
    val dataDB = Storage.getPEvents()
    val dataRDD: RDD[Point] = dataDB.find(
      appId = dsp.appId,
      //channelId = None,
      entityType= Some("user")
      )(sc).map { event =>
      try {
        Point(
          for(i <- 1 to dsp.dimention) yield { 
            event.properties.get[Double](("dim_" + i).toString)
          }
        ) 
      }
      catch {
        case e: Exception => {
          logger.error(s"Failed to get properties.")
          throw e
        }
      }
    }.cache()
    new TrainingData(dataRDD)
  }
}
class TrainingData(val points: RDD[Point]) extends Serializable {
}
case class ActualResult(val cluster: Int) extends Serializable
case class Point(val points_seq: IndexedSeq[Double])