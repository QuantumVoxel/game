package dev.ultreon.quantum.client.world

import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.client.cube
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.util.NamespaceID

class Skybox : Disposable {
  val topColor: Color = Color(0.5f, 0.64f, 0.985f, 1.0f)
  val midColor: Color = Color(0.75f, 0.825f, 0.9456f, 1f)
  val bottomColor: Color = Color(0.75f, 0.825f, 0.9456f, 1f)

  val mesh: Mesh = MeshBuilder().apply {
    begin(VertexAttributes(VertexAttribute.Position()), GL20.GL_TRIANGLES)
    cube(
      0.5f, 0.5f, 0.5f,
      -1f, -1f, -1f
    )
  }.end()

  val transform = Matrix4().setToTranslation(0f, 0f, 0f)

  val shaderProgram: ShaderProgram = ShaderProgram(
    quantum.clientResources.require(NamespaceID.of(path =
      if (gamePlatform.isGL30 || gamePlatform.isWebGL3 || gamePlatform.isGLES3) {
        "shaders/programs/skybox.vert"
      } else "shaders/programs/legacy/skybox.vert")).text,
    quantum.clientResources.require(NamespaceID.of(path =
      if (gamePlatform.isGL30 || gamePlatform.isWebGL3 || gamePlatform.isGLES3) {
        "shaders/programs/skybox.frag"
      } else "shaders/programs/legacy/skybox.frag")).text
  ).also {
    if(!it.isCompiled) {
      logger.error("Failed to compile skybox shader:\n${it.log}")
    }
  }

  fun render(camera: Camera, xRot: Float) {
    if (!shaderProgram.isCompiled) throw RuntimeException("Skybox shader is not compiled")

    shaderProgram.bind()
    transform.setToTranslation(camera.position).rotate(Vector3.Y, xRot)

    shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined)
    shaderProgram.setUniformMatrix("u_worldTrans", transform)
    shaderProgram.setUniformf("u_topColor", topColor)
    shaderProgram.setUniformf("u_midColor", midColor)
    shaderProgram.setUniformf("u_bottomColor", bottomColor)

    mesh.render(shaderProgram, GL20.GL_TRIANGLES)
  }

  override fun dispose() {
    mesh.dispose()
    shaderProgram.dispose()
  }
}
