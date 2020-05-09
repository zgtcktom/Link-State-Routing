import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class Main extends Application {
    LSRCompute lsr;

    ChoiceBox<String> boxSrc;
    Button btnLoad, btnSave, btnSingle, btnAll, btnNew, btnRemove, btnBreak, btnContinue, btnReset;
    TextField textNew, textRemove, textBreak;
    TextArea txtContent, txtStatus;

    BooleanProperty singleStepping = new SimpleBooleanProperty(false);

    private void reset() {
        txtStatus.clear();
        txtStatus.setScrollTop(txtStatus.getScrollTop());
    }

    private void ready(Scene scene) {
        boxSrc = (ChoiceBox<String>) scene.lookup("#box_src");
        btnLoad = (Button) scene.lookup("#btn_load");
        btnSave = (Button) scene.lookup("#btn_save");
        btnSingle = (Button) scene.lookup("#btn_single");
        btnAll = (Button) scene.lookup("#btn_all");
        btnNew = (Button) scene.lookup("#btn_new");
        btnRemove = (Button) scene.lookup("#btn_remove");
        btnBreak = (Button) scene.lookup("#btn_break");
        btnReset = (Button) scene.lookup("#btn_reset");
        textNew = (TextField) scene.lookup("#text_new");
        textRemove = (TextField) scene.lookup("#text_remove");
        textBreak = (TextField) scene.lookup("#text_break");
        txtContent = (TextArea) scene.lookup("#txt_content");
        txtStatus = (TextArea) scene.lookup("#txt_status");
    }

    private void update() {
        txtContent.setText(lsr.network.toString());
        String value = boxSrc.getValue();
        List<String> names = lsr.network.getNames();
        boxSrc.getItems().clear();
        names.forEach(name -> boxSrc.getItems().add(name));

        if (boxSrc.getItems().isEmpty()) boxSrc.setValue(null);
        else if (value != null && names.contains(value)) boxSrc.setValue(value);
        else boxSrc.setValue(names.get(0));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root, 700, 400);
        primaryStage.setTitle("LSRCompute");
        primaryStage.setScene(scene);
        primaryStage.show();
        ready(scene);

        lsr = new LSRCompute() {
            @Override
            public void log(String str, String end) {
                txtStatus.appendText(str + end);
            }
        };
        lsr.useGUI = true;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        btnLoad.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                try {
                    lsr.network.init();
                    lsr.load(file);
                    update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        });

        btnSave.setOnAction(e -> {
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                try {
                    PrintWriter writer = new PrintWriter(file);
                    writer.print(lsr.network);
                    writer.close();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        });

        btnSingle.setOnAction(e -> {
            reset();
            singleStepping.set(true);
            lsr.dijkstra(boxSrc.getValue(), true);
        });

        btnAll.setOnAction(e -> {
            reset();
            lsr.dijkstra(boxSrc.getValue(), false);
        });

        btnNew.setOnAction(e -> {
            reset();
            String input = textNew.getText();
            if (input != null && input.length() > 0) {
                lsr.network.load(input);
                lsr.log("Added node " + input);
                update();
            } else {
                lsr.log("Empty field");
            }
            // H: F:9 E:2
        });

        btnRemove.setOnAction(e -> {
            reset();
            String input = textRemove.getText();
            if (input != null && input.length() > 0) {
                if (lsr.network.has(input)) {
                    lsr.network.remove(input);
                    lsr.log("Removed node " + input);
                } else {
                    lsr.log("Node " + input + " does not exist");
                }
                update();
            } else {
                lsr.log("Empty field");
            }
            // D
        });

        btnBreak.setOnAction(e -> {
            reset();
            String input = textBreak.getText();
            if (input != null && input.length() > 0) {
                String[] names = input.split(">");
                for (int i = 0; i < names.length - 1; i++) {
                    lsr.network.breakLink(names[i], names[i + 1]);
                    lsr.log("Removed link " + names[i] + ">" + names[i + 1]);
                }
                update();
            } else {
                lsr.log("Empty field");
            }
            // C>D
        });

        btnReset.setOnAction(e -> {
            reset();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
