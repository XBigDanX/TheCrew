package game.thecrew.utils;

import game.thecrew.mission.MissionLibrary;
import game.thecrew.model.Mission;
import game.thecrew.model.Task;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DocumentationUtils {

    public static void generateDocumentation() {
        try {
            Class<?> libraryClass = MissionLibrary.class;
            Method[] methods = libraryClass.getDeclaredMethods();
            List<Method> missionMethods = new ArrayList<>();

            for (Method method : methods) {
                if (method.getName().startsWith("getMission") && method.getParameterCount() == 1) {
                    missionMethods.add(method);
                }
            }

            // Sort methods by mission number
            missionMethods.sort(Comparator.comparingInt(m -> {
                String name = m.getName();
                return Integer.parseInt(name.substring("getMission".length()));
            }));

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n");
            html.append("<title>The Crew - Mission Documentation</title>\n");
            html.append("<style>\n");
            html.append("body { font-family: sans-serif; margin: 40px; background-color: #f0f0f0; }\n");
            html.append("h1 { color: #2c3e50; border-bottom: 2px solid #2c3e50; }\n");
            html.append(".mission-card { background: white; padding: 20px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n");
            html.append(".mission-header { display: flex; justify-content: space-between; align-items: center; }\n");
            html.append(".mission-id { font-size: 1.2em; font-weight: bold; color: #e67e22; }\n");
            html.append(".difficulty { color: #7f8c8d; }\n");
            html.append(".description { margin: 15px 0; font-style: italic; color: #34495e; }\n");
            html.append(".tasks { list-style-type: none; padding-left: 0; }\n");
            html.append(".tasks li { padding: 5px 0; border-bottom: 1px solid #eee; }\n");
            html.append(".method-info { font-family: monospace; font-size: 0.9em; color: #888; margin-top: 10px; }\n");
            html.append("</style>\n</head>\n<body>\n");
            html.append("<h1>The Crew - Mission Documentation</h1>\n");

            for (Method method : missionMethods) {
                // Reflective invocation to get mission details
                // We use 4 players as a standard count for documentation
                Mission mission = (Mission) method.invoke(null, 4);

                html.append("<div class='mission-card'>\n");
                html.append("  <div class='mission-header'>\n");
                html.append("    <span class='mission-id'>Mission ").append(mission.getId()).append("</span>\n");
                html.append("    <span class='difficulty'>Difficulty: ").append(mission.getDifficulty()).append("</span>\n");
                html.append("  </div>\n");
                html.append("  <p class='description'>").append(mission.getDescription()).append("</p>\n");
                
                html.append("  <strong>Tasks:</strong>\n");
                html.append("  <ul class='tasks'>\n");
                for (Task task : mission.getTasks()) {
                    html.append("    <li>").append(task.getDescription()).append("</li>\n");
                }
                html.append("  </ul>\n");

                html.append("  <div class='method-info'>Generated from Java method: <code>")
                    .append(java.lang.reflect.Modifier.toString(method.getModifiers()))
                    .append(" ")
                    .append(method.getReturnType().getSimpleName())
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(method.getParameterTypes()[0].getSimpleName())
                    .append(")</code></div>\n");
                html.append("</div>\n");
            }

            html.append("</body>\n</html>");

            Path docDir = Paths.get("documentation");
            if (!Files.exists(docDir)) {
                Files.createDirectories(docDir);
            }
            Files.write(docDir.resolve("missions.html"), html.toString().getBytes());
            System.out.println("Documentation generated in documentation/missions.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateDocumentation();
    }
}
