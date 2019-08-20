import com.google.inject.AbstractModule

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ebean.config.ServerConfig
import io.ebean.EbeanServerFactory
import org.avaje.datasource.DataSourceConfig

class StartModule extends AbstractModule {
  protected def configure(): Unit = {
    // Workaround for a race-condition when initializing the default EbeanServer.
    val config = ConfigFactory.load()
    val ebeanConfig = new ServerConfig()
    val db = new DataSourceConfig()
    db.setDriver(config.getString("db.default.driver"))
    db.setUsername(config.getString("db.default.username"))
    db.setPassword(config.getString("db.default.password"))
    db.setUrl(config.getString("db.default.url"))
    ebeanConfig.setDefaultServer(true)
    ebeanConfig.setDataSourceConfig(db)
    EbeanServerFactory.create(ebeanConfig)

    bind(classOf[ApplicationStart]).asEagerSingleton()
  }
}
