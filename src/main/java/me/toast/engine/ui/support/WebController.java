/*
 * Ultralight Java - Java wrapper for the Ultralight web engine
 * Copyright (C) 2020 - 2021 LabyMedia and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package me.toast.engine.ui.support;

import com.labymedia.ultralight.*;
import com.labymedia.ultralight.config.*;
import com.labymedia.ultralight.gpu.*;
import com.labymedia.ultralight.javascript.*;
import me.toast.engine.Mod;
import me.toast.engine.rendering.Vertex;
import me.toast.engine.ui.HTMLRenderMesh;
import me.toast.engine.ui.input.*;
import me.toast.engine.ui.listener.*;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Class used for controlling the WebGUI rendered on top of the OpenGL GUI.
 */
public class WebController {
    private final UltralightPlatform platform;
    private final CursorAdapter cursorManager;
    private final long window;
    private UltralightRenderer renderer;
    private UltralightView view;
    private UIViewListener viewListener;
    private UILoadListener loadListener;
    private InputAdapter inputAdapter;

    private UltralightOpenGLGPUDriverNative driver;
    private HTMLRenderMesh mesh;

    private long lastJavascriptGarbageCollections;

    /**
     * Constructs a new {@link WebController} and retrieves the platform.
     *
     * @param cursorManager Cursor manager for callbacks on cursor changes
     * @param window        the window handle
     */
    public WebController(CursorAdapter cursorManager, long window) {
        this.cursorManager = cursorManager;
        this.window = window;
        this.platform = UltralightPlatform.instance();

        this.platform.setConfig(
                new UltralightConfig()
                        .forceRepaint(false)
                        .fontHinting(FontHinting.SMOOTH)
        );
        this.platform.usePlatformFontLoader();
        this.platform.setFileSystem(new UltralightStandardFileSystem());
        this.platform.setLogger(new UltralightStandardLogger());
        this.platform.setClipboard(new ClipboardAdapter());
    }

    public void initGPUDriver() {
        this.driver = new UltralightOpenGLGPUDriverNative(this.window, false, GLFW.Functions.GetProcAddress);

        this.platform.setGPUDriver(this.driver);
        this.renderer = UltralightRenderer.create();
        this.renderer.logMemoryUsage();

        this.view = renderer.createView(Mod.Window.Width, Mod.Window.Height,
                new UltralightViewConfig()
                        .isAccelerated(true)
                        .initialDeviceScale(1.0)
                        .isTransparent(true)
        );
        this.viewListener = new UIViewListener(cursorManager);
        this.view.setViewListener(viewListener);
        this.loadListener = new UILoadListener(view);
        this.view.setLoadListener(loadListener);

        this.lastJavascriptGarbageCollections = 0;

        this.inputAdapter = new InputAdapter(view);

        mesh = new HTMLRenderMesh(new Vertex[]{
                new Vertex(new Vector3f(-1, 1, 0)),
                new Vertex(new Vector3f(-1, -1, 0)),
                new Vertex(new Vector3f(1, 1, 0)),
                new Vertex(new Vector3f(1, -1, 0))
        });
    }

    /**
     * Retrieves the input adapter of this web controller.
     *
     * @return The input adapter of this web controller
     */
    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }

    /**
     * Loads the specified URL into this controller.
     *
     * @param url The URL to load
     */
    public void loadURL(String url) {
        this.view.loadURL(url);
    }

    /**
     * Updates and renders the renderer
     */
    public void update() {
        this.renderer.update();
        this.renderer.render();

        if (lastJavascriptGarbageCollections == 0) {
            lastJavascriptGarbageCollections = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastJavascriptGarbageCollections > 1000) {
            System.out.println("Garbage collecting Javascript...");
            try (JavascriptContextLock lock = this.view.lockJavascriptContext()) {
                lock.getContext().garbageCollect();
            }
            lastJavascriptGarbageCollections = System.currentTimeMillis();
        }
    }

    /**
     * Resizes the web view.
     *
     * @param width  The new view width
     * @param height The new view height
     */
    public void resize(int width, int height) {
        this.view.resize(width, height);
    }

    /**
     * Render the current image using OpenGL
     */
    public void render() {
        this.driver.setActiveWindow(this.window);
        glfwMakeContextCurrent(window);
        //These lines cause the screen to go black, some stuff renders from Ultralight if we do this, but text doesn't
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_TRANSFORM_BIT);

        if (this.driver.hasCommandsPending()) {
            //GLFW.glfwMakeContextCurrent(this.window);
            this.driver.drawCommandList();
            //GLFW.glfwSwapBuffers(this.window);
        }

        glPopAttrib();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        this.renderHtmlTexture(this.view);
        glfwMakeContextCurrent(window);

    }

    public boolean getIsLoading() {
        return view.isLoading();
    }

    public void SaveFramebuffer() {
        glActiveTexture(GL_TEXTURE0);
        driver.bindTexture(0, view.renderTarget().getTextureId());

        int format = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        int channels = 4;
        if (format == GL_RGB)
            channels = 3;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_UNSIGNED_BYTE, buffer);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int i = (x + y * width) * channels;

                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = 255;
                if (channels == 4)
                    a = buffer.get(i + 3) & 0xFF;

                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, "PNG", new File("out.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderHtmlTexture(UltralightView view) {
        driver.setActiveWindow(window);
        mesh.Render(view, driver);
    }

    @Deprecated
    private void renderHtmlTexture(UltralightView view, long window) {
        driver.setActiveWindow(window);
        long text = view.renderTarget().getTextureId();
        int width = (int) view.width();
        int height = (int) view.height();
        glClearColor(0, 0, 0, 1);
        glEnable(GL_TEXTURE_2D);
        // Set up the OpenGL state for rendering of a fullscreen quad
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_TRANSFORM_BIT);

        this.driver.bindTexture(0, text);

        glUseProgram(0);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, view.width(), view.height(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        // Disable lighting and scissoring, they could mess up th renderer
        glLoadIdentity();
        glDisable(GL_LIGHTING);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Make sure we draw with a neutral color
        // (so we don't mess with the color channels of the image)
        glColor4f(1, 1, 1, 1);

        glBegin(GL_QUADS);

        // Lower left corner, 0/0 on the screen space, and 0/0 of the image UV
        glTexCoord2f(0, 0);
        glVertex2f(0, 0);

        // Upper left corner
        glTexCoord2f(0, 1);
        glVertex2i(0, height);

        // Upper right corner
        glTexCoord2f(1, 1);
        glVertex2i(width, height);

        // Lower right corner
        glTexCoord2f(1, 0);
        glVertex2i(width, 0);

        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        // Restore OpenGL state
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glDisable(GL_TEXTURE_2D);
        glPopAttrib();
    }
}
