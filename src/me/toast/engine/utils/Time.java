package me.toast.engine.utils;

public class Time {

    public static float timeStarted = System.nanoTime();
    public static float getTime() { return (float)((System.nanoTime() - timeStarted) * 1E-9); }
    public static float deltaTime = 0f;
    private static float endTime, beginTime;

    public static void Init() {
        deltaTime = 0f;
        endTime = 0f;
        beginTime = Time.getTime();
    }

    public static void UpdateDeltaTime() {
        endTime = Time.getTime();
        deltaTime = endTime - beginTime;
        beginTime = Time.getTime();
    }
}
