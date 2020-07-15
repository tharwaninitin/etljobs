package etlflow.steps

import etlflow.etlsteps.DPSparkJobStep
import etlflow.utils.DataprocExecutor
import zio.ZIO
import zio.test._
import zio.test.Assertion._

object DPSparkStepTestSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("EtlFlow Steps")(
      testM("Execute DPSparkJob step") {
        val dpConfig = DataprocExecutor(
          sys.env("DP_PROJECT_ID"),
          sys.env("DP_REGION"),
          sys.env("DP_ENDPOINT"),
          sys.env("DP_CLUSTER_NAME")
        )
        val libs = sys.env("DP_LIBS").split(",").toList

        val step = DPSparkJobStep(
          name        = "DPSparkJobStepExample",
          job_name    = sys.env("DP_JOB_NAME"),
          props       = Map.empty,
          config      = dpConfig,
          main_class  = sys.env("DP_MAIN_CLASS"),
          libs        = libs
        )
        assertM(step.process().foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    )
}