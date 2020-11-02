package org.thp.cortex.services

import java.lang.management.ManagementFactory
import java.util.concurrent.{Executor, Executors, ScheduledExecutorService, TimeUnit}

import scala.collection.JavaConverters._
import akka.dispatch.{Dispatcher, ExecutorServiceDelegate}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
class Monitor @Inject()(configuration: Configuration, ec: ExecutionContext) {
  private val logger: Logger                            = Logger(getClass)
  private val runtime: Runtime                          = Runtime.getRuntime
  private lazy val scheduler: ScheduledExecutorService  = Executors.newSingleThreadScheduledExecutor()
  private val interval: FiniteDuration                  = configuration.getOptional[FiniteDuration]("monitor.interval").getOrElse(1.second)
  private var monitoredExecutors: Seq[(String, AnyRef)] = Seq.empty

  ec match {
    case e: Executor ⇒ monitorExecutor(e, "default")
    case _           ⇒ logger.warn(s"Unknown default executor: $ec (${ec.getClass})")
  }
  scheduler.scheduleAtFixedRate(runnable, interval.toMillis, interval.toMillis, TimeUnit.MILLISECONDS)

  private lazy val runnable = new Runnable {
    override def run(): Unit = {
      logger.trace(s"${runtime.availableProcessors()} cpus, memory: ${runtime.freeMemory()}/${runtime.maxMemory()} (total: ${runtime.totalMemory()})")
      ManagementFactory.getGarbageCollectorMXBeans.asScala.foreach { c ⇒
        logger.trace(s"GC: ${c.getName} ${c.getCollectionCount} execution in ${c.getCollectionTime} milliseconds")
      }
      monitoredExecutors.foreach {
        case (ecName, ec) ⇒ logger.trace(s"Executor $ecName: $ec")
      }
    }
  }

  def monitorExecutor(ec: Executor, ecName: String): Unit = ec match {
    case dispatcher: Dispatcher ⇒
      logger.info(s"Monitoring executor $ecName of type akka.dispatch.Dispatcher")
      // Reflection to access Scala protected method
      val ecGetterForAkkaDispatcher = classOf[Dispatcher].getDeclaredMethod("executorService")
      ecGetterForAkkaDispatcher.setAccessible(true)
      monitorExecutor(ecGetterForAkkaDispatcher.invoke(dispatcher).asInstanceOf[ExecutorServiceDelegate].executor, ecName)

    case _ ⇒
      logger.info(s"Monitoring executor $ecName")
      synchronized(monitoredExecutors = monitoredExecutors :+ (ecName → ec))
  }
}
