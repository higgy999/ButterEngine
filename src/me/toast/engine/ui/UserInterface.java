package me.toast.engine.ui;

import com.labymedia.ultralight.*;
import com.labymedia.ultralight.gpu.*;
import com.labymedia.ultralight.os.OperatingSystem;
import me.toast.engine.Main;
import me.toast.engine.ui.input.*;
import me.toast.engine.ui.support.*;
import me.toast.engine.window.Input;
import me.toast.engine.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Callback;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;

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

        setActiveGUI(new GUI(true, "example.html"));
    }

    public void Update() {
        webController.update();
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

        //webController.loadURL("file:///" + newGUI.pathToHTML);
        webController.loadURL("https://higgy999.github.io");

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
                    Main.class.getResourceAsStream("/example.html"),
                    Paths.get("./example.html"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    Main.class.getResourceAsStream("/example.js"),
                    Paths.get("./example.js"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    Main.class.getResourceAsStream("/style.css"),
                    Paths.get("./style.css"),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
