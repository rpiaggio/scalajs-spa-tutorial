import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import play.sbt.PlayImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys.{scalacOptions, _}


/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */
object Settings {
  /** The name of your application */
  val name = "scalajs-spa"

  /** The version of your application */
  val version = "1.2.0"

  /** Options for the scala compiler */
  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
//    val scala = "2.11.11"
    val scala = "2.12.10"
    val scalaDom = "0.9.7"
    val scalajsReact = "1.4.2"
    val scalaCSS = "0.5.3"
    val scalajsReactCommon = "0.3.2"
    val log4js = "1.4.10"
    val autowire = "0.2.6"
    val booPickle = "1.3.1"
    val diode = "1.1.5"
    val diodeReact = "1.1.5.142"
    val uTest = "0.4.7"

    val react = "16.7.0"
    val jQuery = "1.11.1"
    val bootstrap = "3.3.6"
    val chartjs = "2.9.3"

    val cats = "2.0.0"
    val fs2 = "2.0.0"
    //val crystal = "0.0.3-SNAPSHOT" // For the moment crystal is being developed here. It will eventually be its own project.
    val monocle = "2.0.0"

    val scalajsScripts = "1.1.4"
  }

  /**
   * These dependencies are shared between JS and JVM projects
   * the special %%% function selects the correct version for each project
   */
  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % versions.autowire,
    "io.suzaku" %%% "boopickle" % versions.booPickle
  ))

  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "com.vmunier" %% "scalajs-scripts" % versions.scalajsScripts,
    "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
    "org.webjars" % "bootstrap" % versions.bootstrap % Provided,
    "com.lihaoyi" %% "utest" % versions.uTest % Test,
    guice
  ))

  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "io.github.cquiroz.react" %%% "common" % versions.scalajsReactCommon,
    "io.suzaku" %%% "diode" % versions.diode,
    "io.suzaku" %%% "diode-react" % versions.diodeReact,
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,
    "org.typelevel" %%% "cats-core" % versions.cats,
    "org.typelevel" %%% "cats-effect" % versions.cats,
    "co.fs2" %%% "fs2-core" % versions.fs2,
    // "com.rpiaggio" %%% "crystal" % versions.crystal, // For the moment crystal is being developed here.
    "com.github.julien-truffaut" %%  "monocle-core"  % versions.monocle,
    "com.github.julien-truffaut" %%  "monocle-macro"  % versions.monocle,
    "com.lihaoyi" %%% "utest" % versions.uTest % Test
  ))

  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars.npm" % "react" % versions.react / "umd/react.development.js" minified "umd/react.production.min.js" commonJSName "React",
    "org.webjars.npm" % "react-dom" % versions.react / "umd/react-dom.development.js" minified "umd/react-dom.production.min.js" dependsOn "umd/react.development.js" commonJSName "ReactDOM",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "chartjs" % versions.chartjs / "Chart.js" minified "Chart.min.js",
    "org.webjars" % "log4javascript" % versions.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js"
  ))
}
