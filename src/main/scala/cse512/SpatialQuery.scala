package cse512

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App{
  def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=> {
      var points = pointString.split(",");
      var rectangle = queryRectangle.split(",");
      val px = points(0).toDouble;
      val py = points(1).toDouble;
      val rx1 = rectangle(0).toDouble;
      val ry1 = rectangle(1).toDouble;
      val rx2 = rectangle(2).toDouble;
      val ry2 = rectangle(3).toDouble;

      if (px >= rx1 && px <= rx2 && py >= ry1 && py <= ry2)
        true;
      else
        false;

    })

    val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>{
      var points = pointString.split(",");
      var rectangle = queryRectangle.split(",");
      val px = points(0).toDouble;
      val py = points(1).toDouble;
      val rx1 = rectangle(0).toDouble;
      val ry1 = rectangle(1).toDouble;
      val rx2 = rectangle(2).toDouble;
      val ry2 = rectangle(3).toDouble;

      if (px >= rx1 && px <= rx2 && py >= ry1 && py <= ry2)
        true;
      else
        false;
    })

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>{
      var p1 = pointString1.split(",");
      var p2 = pointString2.split(",");

      val p1x = p1(0).toDouble
      val p1y = p1(1).toDouble
      val p2x = p2(0).toDouble
      val p2y = p2(1).toDouble

      val dist = (p2x-p1x)*(p2x-p1x) + (p2y-p1y)*(p2y-p1y)
      val distGiven = distance*distance
      if (dist <= distGiven)
        true
      else
        false

    })

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
    resultDf.show();

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>{
      var p1 = pointString1.split(",");
      var p2 = pointString2.split(",");

      val p1x = p1(0).toDouble
      val p1y = p1(1).toDouble
      val p2x = p2(0).toDouble
      val p2y = p2(1).toDouble

      val dist = (p2x-p1x)*(p2x-p1x) + (p2y-p1y)*(p2y-p1y)
      val distGiven = distance*distance
      if (dist <= distGiven)
        true
      else
        false
    })
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
    resultDf.show()

    return resultDf.count()
  }
}
