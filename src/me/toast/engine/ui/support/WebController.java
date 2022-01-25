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
import me.toast.engine.ui.input.*;
import me.toast.engine.ui.listener.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL20.*;

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

        this.view = renderer.createView(300, 300,
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
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_TRANSFORM_BIT);

        if (this.driver.hasCommandsPending()) {
            //GLFW.glfwMakeContextCurrent(this.window);
            this.driver.drawCommandList();
            //GLFW.glfwSwapBuffers(this.window);
        }

        glPopAttrib();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        this.renderHtmlTexture(this.view, this.window);
        glfwMakeContextCurrent(window);

    }

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
