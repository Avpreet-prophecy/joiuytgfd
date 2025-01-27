from pyspark.sql import *
from pyspark.sql.functions import *
from pyspark.sql.types import *
from my_pipeline.config.ConfigStore import *
from my_pipeline.functions import *
from prophecy.utils import *

def pipeline(spark: SparkSession) -> None:
    pass

def main():
    spark = SparkSession.builder.enableHiveSupport().appName("my_pipeline").getOrCreate()
    Utils.initializeFromArgs(spark, parse_args())
    spark.conf.set("prophecy.metadata.pipeline.uri", "pipelines/my_pipeline")
    spark.conf.set("spark.default.parallelism", "4")
    spark.conf.set("spark.sql.legacy.allowUntypedScalaUDF", "true")
    registerUDFs(spark)
    
    MetricsCollector.instrument(spark = spark, pipelineId = "pipelines/my_pipeline", config = Config)(pipeline)

if __name__ == "__main__":
    main()
