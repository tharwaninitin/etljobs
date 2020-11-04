package etlflow.log

import org.slf4j.{Logger, LoggerFactory}

trait ApplicationLogger {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
