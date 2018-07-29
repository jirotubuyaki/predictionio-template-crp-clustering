package com.template.crp_clustering

import org.apache.predictionio.controller.P2LAlgorithm
import org.apache.predictionio.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import grizzled.slf4j.Logger
import breeze.linalg._
import breeze.numerics._
import breeze.stats._
import scala.collection.mutable._
import breeze.stats.distributions.MultivariateGaussian
import com.template.crp_clustering._
import scala.util.parsing.json._

case class AlgorithmParams(
  mu: List[Double],
  sigma_table: Double,
  alpha: Double,
  ro_0: Double,
  burn_in: Int,
  iteration: Int
  ) extends Params

class Algorithm(val ap: AlgorithmParams) extends P2LAlgorithm[PreparedData, CRPModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): CRPModel = {
		println("Running the CRP clustering algorithm.")
    var result: Array[Array[Double]] = CRPClustering.crp_gibbs(data, ap.mu, ap.sigma_table, ap.alpha, ap.ro_0, ap.burn_in, ap.iteration)
    new CRPModel(result)
  }
	def predict(model: CRPModel, query: Query): PredictedResult = {
  	val result = model.predict(query)
  	PredictedResult(cluster = result)  
	}
}
class CRPModel(val params: Array[Array[Double]]) extends Serializable {
  def predict(query: Query): Array[Double] = {
    var dim = query.point.size
    println(dim)
    var prob_k: Array[Double] = Array.ofDim(params.length + 1)
    for(j <- 0 to params.length - 1){
      var mu_k = DenseVector.zeros[Double](dim) 
      var sigma_k = DenseMatrix.zeros[Double](dim, dim)
      var data = DenseVector.zeros[Double](dim)
      var count: Int = 0
      for(k <- 0 to dim - 1){
        mu_k(k) = params(j)(k + 2)
        data(k) = query.point(k)
      }
      for(k <- 0 to dim - 1){
        for(l <- 0 to dim - 1){
          sigma_k(k, l) = params(j)(2 + dim + count)
          count += 1
        }
      }
      var mgauss = new MultivariateGaussian(mu_k, sigma_k)
       prob_k(j + 1) = mgauss.pdf(data)
    }
    var max: Double = 0
    for(j <- 0 to params.length - 1){
      if(max < prob_k(j + 1)){
        prob_k(0) = j
        max = prob_k(j + 1)
      }
    }
    return prob_k
  }
}
