package dev.emi.emi.runtime;


public class EmiProfiler {

	public static void push(String name) {
		net.minecraft.util.profiling.Profiler.get().push(name);
	}

	public static void pop() {
		net.minecraft.util.profiling.Profiler.get().pop();
	}

	public static void swap(String name) {
		net.minecraft.util.profiling.Profiler.get().popPush(name);
	}
}
