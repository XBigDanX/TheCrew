package game.thecrew.utils;

import game.thecrew.mission.MissionLibrary;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DocumentationUtils {

    private DocumentationUtils() {}

    public static void generateDocumentation() {

        try {

            Class<?> clazz = MissionLibrary.class;
            Method[] methods = clazz.getDeclaredMethods();

            List<Method> missionMethods = new ArrayList<>();

            for (Method m : methods) {
                if (m.getName().startsWith("getMission")) {
                    missionMethods.add(m);
                }
            }

            StringBuilder html = new StringBuilder();

            html.append("<html><body>");
            html.append("<h1>Mission Documentation</h1>");

            for (Method method : missionMethods) {

                html.append("<hr>");

                html.append("<h2>")
                        .append(Modifier.toString(method.getModifiers()))
                        .append(" ")
                        .append(method.getReturnType().getSimpleName())
                        .append(" ")
                        .append(method.getName())
                        .append("(");

                Parameter[] params = method.getParameters();

                for (int i = 0; i < params.length; i++) {

                    Parameter p = params[i];

                    html.append(p.getType().getSimpleName())
                            .append(" ")
                            .append(p.getName());

                    if (i < params.length - 1) {
                        html.append(", ");
                    }
                }

                html.append(")</h2>");
            }

            html.append("</body></html>");

            Files.writeString(
                    Paths.get("documentation/doc.html"),
                    html.toString()
            );

        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate documentation", e);
        }
    }
}