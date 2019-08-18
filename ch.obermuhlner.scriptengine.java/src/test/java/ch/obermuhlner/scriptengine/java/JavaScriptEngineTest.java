package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.execution.MethodExecutionStrategy;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;

import static org.assertj.core.api.Assertions.*;

public class JavaScriptEngineTest {
    @Test
    public void testAutoCallSupplier() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script implements java.util.function.Supplier<String> {" +
                "   public String get() {" +
                "       return \"Hello\";" +
                "   } " +
                "   public int ignore() {" +
                "       return -1;" +
                "   }" +
                "}");
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    public void testAutoCallRunnable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script implements java.lang.Runnable {" +
                "   public String message;" +
                "   public void run() {" +
                "       message = \"Hello\";" +
                "   } " +
                "   public int ignore() {" +
                "       return -1;" +
                "   }" +
                "}");
        assertThat(result).isEqualTo(null);
        assertThat(engine.get("message")).isEqualTo("Hello");
    }

    @Test
    public void testAutoCallSingularMethod() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage() {" +
                "       return \"Hello\";" +
                "   }" +
                "}");
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    public void failAutoCallSingularMethod() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        assertThatThrownBy(() -> {
            Object result = engine.eval("" +
                    "public class Script {" +
                    "}");
        }).isInstanceOf(ScriptException.class).hasMessageContaining("No method found to execute");

        assertThatThrownBy(() -> {
            Object result = engine.eval("" +
                    "public class Script {" +
                    "   public String getString() {" +
                    "       return \"String\";" +
                    "   }" +
                    "   public int getInt() {" +
                    "       return 42;" +
                    "   }" +
                    "}");
        }).isInstanceOf(ScriptException.class).hasMessageContaining("No method found to execute");
    }

    @Test
    public void testAutoCallSingularStaticMethod() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage(String message) {" +
                "       return \"Message: \" + message;" +
                "   }" +
                "   public static String main() {" +
                "       return new Script().getMessage(\"Hello\");" +
                "   }" +
                "}");
        assertThat(result).isEqualTo("Message: Hello");
    }

    @Test
    public void testMethodCallByArgumentTypes() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byArgumentTypes(
                    clazz,
                    "getMessage",
                    new Class[] { String.class, int.class},
                    "Hello", 42);
        });

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage(String message, int value) {" +
                "       return \"Message: \" + message + value;" +
                "   } " +
                "}");
        assertThat(result).isEqualTo("Message: Hello42");
    }

    @Test
    public void testMethodCallByMatchingArgument() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byMatchingArguments(
                    clazz,
                    "getMessage",
                    "Hello", 42);
        });

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage(String message, int value) {" +
                "       return \"Message: \" + message + value;" +
                "   } " +
                "}");
        assertThat(result).isEqualTo("Message: Hello42");
    }

    @Test
    public void testMethodCallByMatchingArgumentAssignable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byMatchingArguments(
                    clazz,
                    "getMessage",
                    "Hello", 42);
        });

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage(Object message, int value) {" +
                "       return \"Message: \" + message + value;" +
                "   } " +
                "}");
        assertThat(result).isEqualTo("Message: Hello42");
    }

    @Test
    public void testMethodCallByMatchingArgumentNull() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byMatchingArguments(
                    clazz,
                    "getMessage",
                    null, 42);
        });

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage(String message, int value) {" +
                "       return \"Message: \" + message + value;" +
                "   } " +
                "}");
        assertThat(result).isEqualTo("Message: null42");
    }

    @Test
    public void failMethodCallByMatchingArgument() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byMatchingArguments(
                    clazz,
                    "getMessage",
                    "Hello", 42);
        });

        assertThatThrownBy(() -> {
            Object result = engine.eval("" +
                    "public class Script {" +
                    "   public String getMessage(int value1, int value2) {" +
                    "       return \"Message: \" + value1 + value2;" +
                    "   } " +
                    "}");
        }).isInstanceOf(ScriptException.class);
    }

    @Test
    public void failMethodCallByMatchingArgumentNullPrimitive() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

        javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
            return MethodExecutionStrategy.byMatchingArguments(
                    clazz,
                    "getMessage",
                    null, null);
        });

        assertThatThrownBy(() -> {
            Object result = engine.eval("" +
                    "public class Script {" +
                    "   public String getMessage(String message, int value) {" +
                    "       return \"Message: \" + message + value;" +
                    "   } " +
                    "}");
        }).isInstanceOf(ScriptException.class);
    }

    @Ignore
    @Test
    public void testPublicClass() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script {" +
                "   public Object getMessage() {" +
                "       PublicClass result = new ch.obermuhlner.scriptengine.java.JavaScriptEngineTest.PublicClass();" +
                "       result.message = \"Hello\";" +
                "       return result.message;" +
                "   } " +
                "}");
        //assertThat(result).isInstanceOf(PublicClass.class);
        assertThat(result).isEqualTo("Hello");
    }

    public static class PublicClass {
        public String message;
    }
}
