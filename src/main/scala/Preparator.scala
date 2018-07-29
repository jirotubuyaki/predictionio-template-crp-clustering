package com.template.crp_clustering

import org.apache.predictionio.controller.PPreparator
import org.apache.predictionio.data.storage.Event
import org.apache.spark.mllib.linalg.Vector

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import scala.collection._

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(points = trainingData.points)
  }
}

class PreparedData(
  val points: RDD[Point]
) extends Serializable