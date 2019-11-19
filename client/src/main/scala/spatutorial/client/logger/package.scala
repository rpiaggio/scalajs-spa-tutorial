package spatutorial.client

package object logger {
//  private val defaultLogger = LoggerFactory.getLogger("Log")

  val logger = new Logger {
    override def trace(msg: String, e: Exception) = info(msg, e)
    override def trace(msg: String) = info(msg)
    override def debug(msg: String, e: Exception) = info(msg, e)
    override def debug(msg: String) = info(msg)
    override def info(msg: String, e: Exception) = println(msg)
    override def info(msg: String) = println(msg)
    override def warn(msg: String, e: Exception) = info(msg, e)
    override def warn(msg: String) = info(msg)
    override def error(msg: String, e: Exception) = info(msg, e)
    override def error(msg: String) = info(msg)
    override def fatal(msg: String, e: Exception) = info(msg, e)
    override def fatal(msg: String) = info(msg)
    override def enableServerLogging(url: String) = ()
    override def disableServerLogging() = ()
  }

  def log = logger
}
