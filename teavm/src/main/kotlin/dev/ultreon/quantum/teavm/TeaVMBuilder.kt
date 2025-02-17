@file:SkipClass
@file:JvmName("TeaVMBuilder")

package dev.ultreon.quantum.teavm

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import org.intellij.lang.annotations.Language
import org.teavm.tooling.TeaVMTargetType
import org.teavm.tooling.TeaVMTool
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.writeText

fun main() {
  val teaBuildConfiguration = TeaBuildConfiguration().apply {
    assetsPath.add(AssetFileHandle("../assets"))
    assetsPath.add(AssetFileHandle("src/main/res"))
    webappPath = File("build/dist").canonicalPath
    // Register any extra classpath assets here:
    // additionalAssetsClasspathFiles += "dev/ultreon/quantum/asset.extension"

    htmlTitle = "Quantum Voxel"
    useDefaultHtmlIndex = false

    this.showLoadingLogo = true
    this.logoPath = "logo.png"
  }

  // Register any classes or packages that require reflection here:
  // TeaReflectionSupplier.addReflectionClass("dev.ultreon.quantum.reflect")

  val tool: TeaVMTool = TeaBuilder.config(teaBuildConfiguration)
  tool.mainClass = "dev.ultreon.quantum.teavm.TeaVMLauncher"
  tool.optimizationLevel = TeaVMOptimizationLevel.SIMPLE
  tool.targetType = TeaVMTargetType.JAVASCRIPT
  tool.setObfuscated(false)

  @Suppress("HtmlUnknownTarget")
  @Language("HTML")
  val text = """
      |<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |    <title>Quantum Voxel</title>
      |    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
      |
      |    <link rel="manifest" href="/manifest.json">
      |    <link rel="icon" href="/favicon.ico">
      |    <style>
      |        body {
      |          justify-content: center;
      |          align-items: center;
      |          background: #000;
      |          height: 100vh;
      |          margin: 0;
      |          padding: 0;
      |          overflow: hidden
      |        }
      |        #progress {
      |          position: fixed;
      |          display: block;
      |          box-sizing: border-box;
      |          position: absolute;
      |          z-index: 1;
      |          top: 50%;
      |          left: 50%;
      |          width: 500px;
      |          height: 80px;
      |          margin-top: -40px;
      |          margin-left: -250px;
      |        }
      |        #progress-img {
      |          padding-bottom: 5px;
      |        }
      |        #progress-box {
      |          background: rgba(255,255,255,0.1);
      |          justify-content: flex-start;
      |          border: 2px solid #fff;
      |          align-items: center;
      |          position: relative;
      |          padding: 0px 5px;
      |          display: flex;
      |          height: 30px;
      |          width: 487px;
      |          float: left;
      |          clear: both;
      |        }
      |        #progress-bar {
      |          display: block;
      |          box-shadow: 0 10px 30px -10px #fff;
      |          background: #fff;
      |          height: 20px;
      |          width: 0%;
      |        }
      |    </style>
      |</head>
      |<body oncontextmenu="return false">
      |<div>
      |    <div id="progress">
      |        <img id="progress-img" src="logo.png" alt="gdx-teavm">
      |        <div id="progress-box">
      |            <div id="progress-bar"></div>
      |        </div>
      |    </div>
      |    <canvas id="canvas" width="800" height="600"></canvas>
      |</div>
      |<script>
      |    var maxPercentage = 25;
      |    var delay = 500;
      |    var bar = document.getElementById("progress-bar");
      |    if (bar) {
      |      var barStyle = bar.style;
      |      barStyle.width = "0%";
      |      function barUpdater() {
      |        var percentage = parseInt(barStyle.width, 10);
      |        if (percentage < maxPercentage) {
      |          percentage++;
      |          barStyle.width = percentage + "%";
      |          delay += 25;
      |          setTimeout(barUpdater, delay);
      |        }
      |      }
      |    }
      |    setTimeout(barUpdater, delay);
      |</script>
      |<script type="text/javascript" charset="utf-8" src="teavm/app.js"></script>
      |<script>
      |    main()
      |</script>
      |</body>
      |</html>
    """.trimMargin()
  Path("build/dist/webapp/index.html").writeText(text)

  TeaBuilder.build(tool)
}
