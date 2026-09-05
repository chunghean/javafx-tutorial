import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Duke duke;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private Image dukeImage = new Image(this.getClass().getResourceAsStream("/images/DaDuke.png"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /** Injects the Duke instance */
    public void setDuke(Duke d) {
        duke = d;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        playSendSound();
        String response = duke.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getDukeDialog(response, dukeImage)
        );
        userInput.clear();
    }

    /** Plays a short notification sound without blocking the JavaFX application thread. */
    private void playSendSound() {
        Thread soundThread = new Thread(() -> {
            try {
                float sampleRate = 44100;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();

                    int duration = (int) (sampleRate * 0.08);
                    byte[] samples = new byte[duration];
                    for (int i = 0; i < samples.length; i++) {
                        double time = i / sampleRate;
                        double envelope = 1.0 - (double) i / samples.length;
                        samples[i] = (byte) (Math.sin(2 * Math.PI * 880 * time) * 80 * envelope);
                    }

                    line.write(samples, 0, samples.length);
                    line.drain();
                }
            } catch (LineUnavailableException ignored) {
                // Continue silently when the device has no available audio output.
            }
        }, "send-sound");
        soundThread.setDaemon(true);
        soundThread.start();
    }
}
