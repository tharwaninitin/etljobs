package etlflow.webserver.api

import caliban.CalibanError.ExecutionError
import doobie.hikari.HikariTransactor
import etlflow.log.{JobRun, StepRun}
import etlflow.utils.EtlFlowHelper._
import etlflow.utils.db.Query
import etlflow.utils.Executor._
import etlflow.utils.{EtlFlowUtils, UtilityFunctions => UF}
import etlflow.{EtlJobName, EtlJobProps, BuildInfo => BI}
import scalacache.Cache
import zio._
import zio.blocking.Blocking
import zio.stream.ZStream
import scala.reflect.runtime.universe.TypeTag

trait EtlFlowService extends EtlFlowUtils {

  def runEtlJobDataProc(args: EtlJobArgs, transactor: HikariTransactor[Task], config: DATAPROC, sem: Semaphore): Task[EtlJob]
  def runEtlJobKubernetes(args: EtlJobArgs, transactor: HikariTransactor[Task], config: KUBERNETES, sem: Semaphore): Task[EtlJob]
  def runEtlJobLocal(args: EtlJobArgs, transactor: HikariTransactor[Task], sem: Semaphore): Task[EtlJob]
  def runEtlJobLocalSubProcess(args: EtlJobArgs, transactor: HikariTransactor[Task], config: LOCAL_SUBPROCESS, sem: Semaphore): Task[EtlJob]

  def liveHttp4s[EJN <: EtlJobName[EJP] : TypeTag, EJP <: EtlJobProps : TypeTag](
      transactor: HikariTransactor[Task],
      cache: Cache[String],
      cronJobs: Ref[List[CronJob]],
      jobSemaphores: Map[String, Semaphore],
      jobs: List[EtlJob]
    ): ZLayer[Blocking, Throwable, EtlFlowHas] = ZLayer.fromEffect{
    for {
      subscribers           <- Ref.make(List.empty[Queue[EtlJobStatus]])
      activeJobs            <- Ref.make(0)
      etl_job_name_package  = UF.getJobNamePackage[EJN] + "$"
      mb                    = 1024*1024
      javaRuntime           = java.lang.Runtime.getRuntime
    } yield new EtlFlow.Service {

      override def getJobs: ZIO[EtlFlowHas, Throwable, List[Job]] = {
        getJobsFromDb[EJN,EJP](transactor,etl_job_name_package)
      }

      override def runJob(args: EtlJobArgs): ZIO[EtlFlowHas, Throwable, EtlJob] = {
        UF.getEtlJobName[EJN](args.name,etl_job_name_package).getActualProperties(Map.empty).job_deploy_mode match {
          case LOCAL_SUBPROCESS(script_path, heap_min_memory, heap_max_memory) =>
            logger.info("Running job in local sub-process mode ")
            runEtlJobLocalSubProcess(args, transactor,LOCAL_SUBPROCESS(script_path, heap_min_memory, heap_max_memory), jobSemaphores(args.name))
          case LOCAL =>
            logger.info("Running job in local in-process mode ")
            runEtlJobLocal(args, transactor, jobSemaphores(args.name))
          case DATAPROC(project, region, endpoint, cluster_name) =>
            logger.info("Dataproc parameters are : " + project + "::" + region + "::"  + endpoint +"::" + cluster_name)
            runEtlJobDataProc(args, transactor, DATAPROC(project, region, endpoint, cluster_name), jobSemaphores(args.name))
          case LIVY(_) =>
            logger.error("Deploy mode livy not yet supported")
            Task.fail(ExecutionError("Deploy mode livy not yet supported"))
          case KUBERNETES(imageName, nameSpace, envVar, containerName, entryPoint, restartPolicy) =>
            logger.info("KUBERNETES parameters are : " + imageName + "::" + envVar + "::" + nameSpace + "::" + containerName + "::" + entryPoint + "::" + restartPolicy)
            runEtlJobKubernetes(args, transactor, KUBERNETES(imageName,nameSpace, envVar), jobSemaphores(args.name))
        }
      }

      override def getDbStepRuns(args: DbStepRunArgs): ZIO[EtlFlowHas, Throwable, List[StepRun]] = {
        Query.getDbStepRuns(args,transactor)
      }

      override def getDbJobRuns(args: DbJobRunArgs): ZIO[EtlFlowHas, Throwable, List[JobRun]] = {
        Query.getDbJobRuns(args,transactor)
      }

      override def updateJobState(args: EtlJobStateArgs): ZIO[EtlFlowHas, Throwable, Boolean] = {
        Query.updateJobState(args,transactor)
      }

      override def login(args: UserArgs): ZIO[EtlFlowHas, Throwable, UserAuth] =  {
        Query.login(args,transactor,cache)
      }

      override def getInfo: ZIO[EtlFlowHas, Throwable, EtlFlowMetrics] = {
        for {
          x <- activeJobs.get
          y <- subscribers.get
          z <- cronJobs.get
        } yield EtlFlowMetrics(
          x,
          y.length,
          jobs.length,
          z.length,
          used_memory = ((javaRuntime.totalMemory - javaRuntime.freeMemory) / mb).toString,
          free_memory = (javaRuntime.freeMemory / mb).toString,
          total_memory = (javaRuntime.totalMemory / mb).toString,
          max_memory = (javaRuntime.maxMemory / mb).toString,
          current_time = UF.getCurrentTimestampAsString(),
          build_time = BI.builtAtString
        )
      }

      override def getCurrentTime: ZIO[EtlFlowHas, Throwable, CurrentTime] = {
        UIO(CurrentTime(current_time = UF.getCurrentTimestampAsString()))
      }

      override def addCredentials(args: CredentialsArgs): ZIO[EtlFlowHas, Throwable, Credentials] = {
        Query.addCredentials(args,transactor)
      }

      override def updateCredentials(args: CredentialsArgs): ZIO[EtlFlowHas, Throwable, Credentials] = {
        Query.updateCredentials(args,transactor)
      }

      override def addCronJob(args: CronJobArgs): ZIO[EtlFlowHas, Throwable, CronJob] = {
        Query.addCronJob(args,transactor)
      }

      override def updateCronJob(args: CronJobArgs): ZIO[EtlFlowHas, Throwable, CronJob] = {
        Query.updateCronJob(args,transactor)
      }

      override def notifications: ZStream[EtlFlowHas, Nothing, EtlJobStatus] = ZStream.unwrap {
        for {
          queue <- Queue.unbounded[EtlJobStatus]
          _     <- UIO(logger.info(s"Starting new subscriber"))
          _     <- subscribers.update(queue :: _)
        } yield ZStream.fromQueue(queue).ensuring(queue.shutdown)
      }
    }
  }
}