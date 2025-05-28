package me.anjoismysign.img2txt;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Img2txt {

    /**
     * Extracts text from an image file using a given language model and prompt.
     * <p>
     * This method accepts a local image file (JPEG or PNG), converts it to PNG if necessary,
     * encodes the image as Base64, sends it along with a prompt to a chat-based language model,
     * and returns the extracted textual response.
     * </p>
     *
     * @param source The source image file (JPG, JPEG, or PNG) from which to extract text.
     * @param prompt A textual prompt describing what the model should do with the image (e.g., "Describe this image", "Extract any visible text").
     * @param model  The {@link ChatLanguageModel} used to process the image and return a textual response.
     * @return A {@link String} containing the AI-generated textual description or transcription of the image.
     * @throws IOException If there is an error reading the image or writing a converted PNG file.
     */
    public static String extractTextFromLocalSource(@NotNull File source,
                                                    @NotNull String prompt,
                                                    @NotNull ChatLanguageModel model) throws IOException {

        //convert .jpg, .jpeg to png
        String sourceAbsolutePath = source.getAbsolutePath();
        if (sourceAbsolutePath.endsWith(".jpg") || sourceAbsolutePath.endsWith(".jpeg")){
            sourceAbsolutePath = sourceAbsolutePath.replace(".jpg",".png").replace(".jpeg",".png");
            File preexistent = new File(sourceAbsolutePath);
            if (preexistent.isFile()){
                return extractTextFromLocalPng(source,prompt,model);
            }

            BufferedImage image = ImageIO.read(source);

            source = new File(sourceAbsolutePath);
            ImageIO.write(image, "png", source);
        }

        return extractTextFromLocalPng(source,prompt,model);

    }

    public static String extractTextFromLocalPng(@NotNull File source,
                                                 @NotNull String prompt,
                                                 @NotNull ChatLanguageModel model) throws IOException {
        //initialize the ImageContent
        byte[] imageBytes = Files.readAllBytes(Paths.get(source.getAbsolutePath()));
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        ImageContent imageContent = ImageContent.from(base64Data, "image/png");

        //initialize the UserMessage
        UserMessage userMessage = UserMessage.from(imageContent);

        //get output
        ChatResponse chatResponse = model.chat(new SystemMessage(prompt),userMessage);
        return chatResponse.aiMessage().text();
    }

}
