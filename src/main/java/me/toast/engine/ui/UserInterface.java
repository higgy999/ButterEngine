package me.toast.engine.ui;

import com.labymedia.ultralight.*;
import com.labymedia.ultralight.gpu.*;
import com.labymedia.ultralight.os.OperatingSystem;
import me.toast.engine.Mod;
import me.toast.engine.ui.input.*;
import me.toast.engine.ui.support.*;
import me.toast.engine.window.Input;
import me.toast.engine.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public class UserInterface {

    private GUI activeGUI;

    private final long window;
    private final Input input;
    private final CursorAdapter cursorManager;
    private final InputAdapter inputAdapter;

    public final WebController webController;

    public UserInterface(Window windowObject) {
        try {
            extractResources();
            Path ultralightNativesDir = Paths.get("./bin");
            
            UltralightJava.extractNativeLibrary(ultralightNativesDir);
            UltralightGPUDriverNativeUtil.extractNativeLibrary(ultralightNativesDir);

            OperatingSystem operatingSystem = OperatingSystem.get();
            List<String> libs = List.of(
                    "glib-2.0-0",
                    "gobject-2.0-0",
                    "gmodule-2.0-0",
                    "gio-2.0-0",
                    "gstreamer-full-1.0",
                    "gthread-2.0-0"
            );

            for (String lib : libs) {
                System.load(ultralightNativesDir.resolve(operatingSystem.mapLibraryName(lib)).toAbsolutePath().toString());
            }

            UltralightJava.load(ultralightNativesDir);
            UltralightGPUDriverNativeUtil.load(ultralightNativesDir);

        } catch (UnsatisfiedLinkError | UltralightLoadException e) {
            throw new IllegalStateException("Unable to load Ultralight natives!", e);
        }

        this.window = windowObject.ID;
        this.input = windowObject.InputEvents;

        // Set up various internal controllers
        this.cursorManager = new CursorAdapter(window);
        this.webController = new WebController(cursorManager, window);

        webController.initGPUDriver();
        inputAdapter = webController.getInputAdapter();

        inputAdapter.focusCallback(window, glfwGetWindowAttrib(window, GLFW_FOCUSED) != 0);
        SetScaling(window);

        setActiveGUI(new GUI(true, "example.html"));
    }

    public void SetScaling(long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Update window size for the first time
            IntBuffer sizeBuffer = stack.callocInt(2);

            // Retrieve the size into the int buffer
            glfwGetWindowSize(window, sizeBuffer.slice().position(0), sizeBuffer.slice().position(1));

            // Update the size
            //updateSize(window, sizeBuffer.get(0), sizeBuffer.get(1));
            glViewport(0, 0, sizeBuffer.get(0), sizeBuffer.get(1));
            webController.resize(sizeBuffer.get(0), sizeBuffer.get(1));

            /*
             * Following snippet disabled due to GLFW bug, glfwGetWindowContentScale returns invalid values!
             *
             * See https://github.com/glfw/glfw/issues/1811.
             */
            // Update scale for the first time
            // FloatBuffer scaleBuffer = stack.callocFloat(2);

            // Retrieve the scale into the float buffer
            // glfwGetWindowContentScale(window,
            //        (FloatBuffer) scaleBuffer.slice().position(0), (FloatBuffer) scaleBuffer.slice().position(1));

            // Retrieve framebuffer size for scale calculation
            IntBuffer framebufferSizeBuffer = stack.callocInt(2);

            // Retrieve the size into the int buffer
            glfwGetFramebufferSize(window, framebufferSizeBuffer.slice().position(0), sizeBuffer.slice().position(1));

            // Calculate scale
            float xScale = ((float) (framebufferSizeBuffer.get(0))) / ((float) (sizeBuffer.get(0)));
            float yScale = ((float) (framebufferSizeBuffer.get(1))) / ((float) (sizeBuffer.get(1)));

            // Fix up scale in case it gets corrupted... somehow
            if (xScale == 0.0f) {
                xScale = 1.0f;
            }

            if (yScale == 0.0f) {
                yScale = 1.0f;
            }

            // Update the scale
            inputAdapter.windowContentScaleCallback(window, xScale, yScale);
        }
    }

    public void Update() {
        //webController.update();
    }

    public void Render() {
        webController.render();
    }

    public void Cleanup() {
        // Clean up native resources
        cursorManager.cleanup();
    }

    public void setActiveGUI(GUI newGUI) {
        activeGUI = newGUI;

        if (newGUI.takesControls) {
            setCallback(GLFW::glfwSetWindowContentScaleCallback, inputAdapter::windowContentScaleCallback);
            setCallback(GLFW::glfwSetKeyCallback, inputAdapter::keyCallback);
            setCallback(GLFW::glfwSetCharCallback, inputAdapter::charCallback);
            setCallback(GLFW::glfwSetCursorPosCallback, inputAdapter::cursorPosCallback);
            setCallback(GLFW::glfwSetMouseButtonCallback, inputAdapter::mouseButtonCallback);
            setCallback(GLFW::glfwSetScrollCallback, inputAdapter::scrollCallback);
            setCallback(GLFW::glfwSetWindowFocusCallback, inputAdapter::focusCallback);
        } else {
            input.KeyboardCallback.set(window);
            input.CharCallback.set(window);
            input.MouseMoveCallback.set(window);
            input.MouseButtonsCallback.set(window);
            input.MouseScrollCallback.set(window);
            input.WindowFocusCallback.set(window);
        }

        webController.loadURL("https://higgy999.github.io");
        //webController.loadURL("file:///" + newGUI.pathToHTML);
    }

    /**
     * Sets a GLFW callback and frees the old callback if it exists.
     *
     * @param setter   The function to use for setting the new callback
     * @param newValue The new callback
     * @param <T>      The type of the new callback
     * @param <C>      The type of the old callback
     */
    private <T, C extends Callback> void setCallback(Function<T, C> setter, T newValue) {
        C oldValue = setter.apply(newValue);
        if (oldValue != null) {
            oldValue.free();
        }
    }

    /**
     * Sets a GLFW callback and frees the old callback if it exists.
     *
     * @param setter   The function to use for setting the new callback
     * @param newValue The new callback
     * @param <T>      The type of the new callback
     * @param <C>      The type of the old callback
     */
    private <T, C extends Callback> void setCallback(BiFunction<Long, T, C> setter, T newValue) {
        C oldValue = setter.apply(window, newValue);
        if (oldValue != null) {
            oldValue.free();
        }
    }

    /**
     * Helper function to set up the run directory with jar resources.
     * TEMPORARY!
     */
    public static void extractResources() {
        try {
            Files.copy(
                    UserInterface.class.getResourceAsStream("/example.html"),
                    Paths.get("./example.html"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    UserInterface.class.getResourceAsStream("/example.js"),
                    Paths.get("./example.js"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    UserInterface.class.getResourceAsStream("/style.css"),
                    Paths.get("./style.css"),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
