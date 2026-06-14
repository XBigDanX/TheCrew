package game.thecrew.utils;

import game.thecrew.exception.GamePersistingException;
import game.thecrew.model.GameState;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtils {

    private static final String ERROR_HEADER = "Error";

    private FileUtils() {
    }

    public static void save(GameState state) {
        FileChooser saveFileChooser = new FileChooser();
        saveFileChooser.setTitle("Save Game");
        File datDir = new File("dat");
        if (!datDir.exists()) {
            datDir.mkdir();
        }
        saveFileChooser.setInitialDirectory(datDir);
        saveFileChooser.setInitialFileName("game_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"))
                + ".dat");

        File selectedFile = saveFileChooser.showSaveDialog(null);
        if (selectedFile == null) {
            return;
        }
        String fileName = selectedFile.getAbsolutePath();

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream(fileName))) {
            objectOutputStream.writeObject(state);
            DialogUtils.showDialog("Game Saved",
                    "Game saved successfully",
                    "Game Saved",
                    Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            DialogUtils.showDialog("Save Game Error",
                    "Error saving game",
                    ERROR_HEADER,
                    Alert.AlertType.ERROR);
            throw new GamePersistingException("An error occurred while saving the game", e);
        }
    }

    public static GameState load() {
        FileChooser loadFileChooser = new FileChooser();
        loadFileChooser.setTitle("Load Game");
        File datDir = new File("./dat");
        if (!datDir.exists()) {
            datDir.mkdir();
        }
        loadFileChooser.setInitialDirectory(datDir);

        File selectedFile = loadFileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return null;
        }
        String fileName = selectedFile.getAbsolutePath();

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(fileName))) {
            GameState state = (GameState) objectInputStream.readObject();

            DialogUtils.showDialog("Game Loaded",
                    "Game loaded successfully",
                    "Game Loaded", Alert.AlertType.INFORMATION);
            return state;
        } catch (IOException | ClassNotFoundException e) {
            DialogUtils.showDialog("Load Game Error",
                    "Error loading game",
                    ERROR_HEADER,
                    Alert.AlertType.ERROR);
            return null;
        }
    }
}
