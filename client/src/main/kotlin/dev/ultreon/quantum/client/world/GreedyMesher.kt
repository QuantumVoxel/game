package dev.ultreon.quantum.client.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.id
import dev.ultreon.quantum.world.SIZE


data class Quad(val x: Int, val y: Int, val z: Int, val width: Int, val height: Int, val depth: Int, val block: Block)

class GreedyMesher {

  fun meshChunk(chunk: ClientChunk): List<Quad> {
    val quads = mutableListOf<Quad>()

    for (axis in 0..2) {
      val u = (axis + 1) % 3
      val v = (axis + 2) % 3

      val mask = Array(SIZE) { Array(SIZE) { Blocks.air } }

      val x = IntArray(SIZE)
      val q = IntArray(SIZE)
      q[axis] = 1

      for (z in -1 until SIZE) {
        val n = Array(SIZE) { Array(SIZE) { Blocks.air } }

        for (v in 0 until SIZE) {
          for (u in 0 until SIZE) {
            val block1 = if (x[axis] >= 0) chunk[x[0], x[1], x[2]] else Blocks.air
            val block2 =
              if (x[axis] < SIZE - 1) chunk[x[0] + q[0], x[1] + q[1], x[2] + q[2]] else Blocks.air

            val visible = block1.id != block2.id && (block1.isOpaque || block2.isOpaque)
            n[x[u]][x[v]] = if (visible) block1 else Blocks.air
          }
        }

        for (j in 0 until SIZE) {
          var i = 0
          while (i < SIZE) {
            if (n[i][j] != Blocks.air) {
              val block = n[i][j]
              var width = 1

              while (i + width < SIZE && n[i + width][j].id == block.id) {
                width++
              }

              var height = 1
              var done = false
              while (j + height < SIZE && !done) {
                for (k in 0 until width) {
                  if (n[i + k][j + height].id != block.id) {
                    done = true
                    break
                  }
                }
                if (!done) {
                  height++
                }
              }

              for (h in 0 until height) {
                for (w in 0 until width) {
                  n[i + w][j + h] = Blocks.air
                }
              }

              val x0 = x[0]
              val y0 = x[1]
              val z0 = x[2]
              x[axis] = x0 + if (axis == 0) i else 0
              x[u] = y0 + if (u == 1) i else 0
              x[v] = z0 + if (v == 2) j else 0
              val du = if (u == 1) width else 1
              val dv = if (v == 2) height else 1

              quads.add(Quad(x[0], x[1], x[2], du, dv, 1, block))

              i += width
            } else {
              i++
            }
          }
        }
      }
    }
    return quads
  }


  fun buildMeshFromQuads(quads: List<Quad>, modelBuilder: ModelBuilder, material: Material): ModelBuilder {
    // Start building the model
    val partBuilder: MeshPartBuilder = QuantumVoxel {
      modelBuilder.part(
        "meshPart",
        com.badlogic.gdx.graphics.GL20.GL_TRIANGLES,
        VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorPacked()),
        material
      )
    }

    quads.forEach { quad ->
      addQuadToMeshPart(quad, partBuilder)
    }

    return modelBuilder
  }

  private fun addQuadToMeshPart(quad: Quad, partBuilder: MeshPartBuilder) {
    val color = when (quad.block) {
      Blocks.air -> Color(0f, 0f, 0f, 0f)
      Blocks.grass -> Color(0f, 0.6f, 0.1f, 1f)
      Blocks.soil -> Color(0.6f, 0.4f, 0.2f, 1f)
      Blocks.stone -> Color(0.4f, 0.4f, 0.4f, 1f)
      Blocks.sand -> Color(1f, 1f, 0.8f, 1f)
      Blocks.iron -> Color(0.9f, 0.9f, 0.9f, 1f)
      Blocks.cobblestone -> Color(0.6f, 0.6f, 0.6f, 1f)
      Blocks.water -> Color(0.3f, 0.7f, 1f, 0.6f)
      Blocks.snowyGrass -> Color(.9f, .98f, 1f, 1f)
      else -> Color(1f, 0f, 1f, 1f)
    }

    val p1 = VertexInfo().setPos(quad.x.toFloat(), quad.y.toFloat(), quad.z.toFloat()).setCol(color).setNor(0f, 0f, 1f)
    val p2 = VertexInfo().setPos((quad.x + quad.width).toFloat(), quad.y.toFloat(), quad.z.toFloat()).setCol(color).setNor(0f, 0f, 1f)
    val p3 = VertexInfo().setPos((quad.x + quad.width).toFloat(), (quad.y + quad.height).toFloat(), quad.z.toFloat()).setCol(color).setNor(0f, 0f, 1f)
    val p4 = VertexInfo().setPos(quad.x.toFloat(), (quad.y + quad.height).toFloat(), quad.z.toFloat()).setCol(color).setNor(0f, 0f, 1f)

    // Add two triangles for the quad
    partBuilder.rect(p1, p2, p3, p4)
  }
}
