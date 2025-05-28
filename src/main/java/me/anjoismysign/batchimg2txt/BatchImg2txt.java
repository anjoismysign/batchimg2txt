package me.anjoismysign.batchimg2txt;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import me.anjoismysign.aesthetic.DirectoryAssistant;
import me.anjoismysign.hahaswing.BubbleFactory;
import me.anjoismysign.img2txt.Img2txt;
import org.jetbrains.annotations.NotNull;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BatchImg2txt {

    public static void main(String[] args) {
        Properties properties = new Properties();
        File parent = new File(new File("").getAbsolutePath());
        File propertiesFile = new File(parent, "batchimg2txt.properties");

        String prompt;
        String key;
        String modelName;

        try (FileInputStream input = new FileInputStream(propertiesFile)) {
            properties.load(input);
            prompt = properties.getProperty("model.google.prompt");
            key = properties.getProperty("model.google.key");
            modelName = properties.getProperty("model.google.name");
        } catch (IOException exception) {
            error(exception);
            return;
        }

        ChatLanguageModel model = GoogleAiGeminiChatModel
                .builder()
                .apiKey(key)
                .modelName(modelName)
                .build();

        run(prompt,model);
    }

    public static void run(@NotNull String prompt,
                           @NotNull ChatLanguageModel model){
        File[] sourceFile = new File[1];

        BubbleFactory.getInstance().controller(
                        null,
                        "batchimg2txt",
                        new ImageIcon(Objects.requireNonNull(BatchImg2txt.class.getResource("/icon.png")))
                                .getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH),
                        false,
                        file -> {
                            sourceFile[0] = file;
                        })
                .onBlow(anjoPane -> {
                    if (anjoPane.didCancel()) {
                        System.exit(0);
                        return;
                    }

                    File droppedFile = sourceFile[0];
                    if (droppedFile == null) {
                        JOptionPane.showMessageDialog(null, "File not dropped", "Error", JOptionPane.ERROR_MESSAGE);
                        run(prompt, model);
                        return;
                    }
                    if (!droppedFile.isDirectory()){
                        JOptionPane.showMessageDialog(null, "File is not a directory", "Error", JOptionPane.ERROR_MESSAGE);
                        run(prompt, model);
                        return;
                    }

                    File txtDirectory = new File(new File("")
                            .getAbsolutePath(),
                            droppedFile.getName());
                    txtDirectory.mkdirs();

                    List<String> pages;

                    try {
                        pages = batch(droppedFile, prompt, model);
                    } catch (IOException exception) {
                        error(exception);
                        run(prompt,model);
                        return;
                    }

                    int index = 0;
                    for (String line : pages) {
                        File txtFile = new File(txtDirectory, index+".txt");
                        if (!txtFile.isFile()) {
                            try {
                                txtFile.createNewFile();
                            } catch (IOException exception) {
                                error(exception);
                                index++;
                                continue;
                            }
                        }
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
                            writer.write(line);
                        } catch (IOException exception) {
                            error(exception);
                        }
                        index++;
                    }

                    run(prompt,model);
                });
    }

    public static void error(@NotNull Throwable throwable){
        JOptionPane.showMessageDialog(null, throwable.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
    }

    public static List<String> batch(@NotNull File directory,
                                     @NotNull String prompt,
                                     @NotNull ChatLanguageModel model) throws IOException {
        if (!directory.isDirectory())
            throw new FileNotFoundException("Not a directory");
        DirectoryAssistant assistant = DirectoryAssistant.of(directory);
        Collection<File> files = assistant.listRecursively("png", "jpg", "jpeg");
        List<String> pages = new ArrayList<>();
        files.forEach(source -> {
            try {
                String page = Img2txt.extractTextFromLocalSource(source, prompt, model);
                pages.add(page);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
        return pages;
    }
}
