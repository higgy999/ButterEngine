package me.toast.engine.ui;

import com.labymedia.ultralight.UltralightView;
import com.labymedia.ultralight.gpu.UltralightOpenGLGPUDriverNative;
import me.toast.engine.AssetPool;
import me.toast.engine.rendering.*;
import org.joml.*;

import static org.lwjgl.opengl.GL33.*;

public class HTMLRenderMesh {

    public BufferObject.VAO VAO;
    public BufferObject[] bufferObjects;

    final Shader shader;

    //Useful for getting information about the arrays //Can't do anything else
    final Vertex[] vertices;

    public HTMLRenderMesh(Vertex[] vertices) {
        this.vertices = vertices;
        this.shader = AssetPool.getShader("HTMLTexRender");

        this.bufferObjects = new BufferObject[1];

        Create();
    }

    //Meant to be overridden
    public void Create() {
        VAO = new BufferObject.VAO();
        VAO.Bind();
            bufferObjects[0] = new BufferObject.VBO(vertices, 0);
        VAO.Unbind();
    }

    //Finally got rendering simplified to one universal method! We'll see how long that lasts //4-21-22 Did not last
    public void Render(UltralightView view, UltralightOpenGLGPUDriverNative driver) {
        VAO.Bind();
            enableVertexAttrib();
                glActiveTexture(GL_TEXTURE0);
                long text = view.renderTarget().getTextureId();
                driver.bindTexture(0, text);
                    shader.Bind();
                        DrawElements();
                    shader.Unbind();
                glActiveTexture(GL_TEXTURE0);
                driver.bindTexture(0, 0);
            disableVertexAttrib();
        VAO.Unbind();
    }

    //This really isn't needed, just moving OpenGL stuff out of each Mesh class for simplicity
    public void DrawElements() { glDrawArrays(GL_TRIANGLE_STRIP, 0, vertices.length); }

    public void enableVertexAttrib() {
        for (int i = 0; i < bufferObjects.length; i++) {
            glEnableVertexAttribArray(i);
        }
    }

    public void disableVertexAttrib() {
        for (int i = 0; i < bufferObjects.length; i++)
            glDisableVertexAttribArray(i);
    }

    public void Cleanup() {
        for (BufferObject object : bufferObjects)
            object.Cleanup();
        VAO.Cleanup();

        AssetPool.Cleanup(shader);
    }
}
