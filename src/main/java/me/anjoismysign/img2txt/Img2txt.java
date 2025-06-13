package me.anjoismysign.img2txt;

import dev.langchain4j.data.message.ImageContent;
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

/**
 * Utility class for converting images to text using a language model.
 * <p>
 * Provides methods to process local image files (JPG, JPEG, PNG), convert them to PNG if necessary,
 * and send them along with a prompt to a ChatLanguageModel for text extraction or description.
 * </p>
 *
 * <p>Key methods:</p>
 * <ul>
 *   <li>{@link #img2txtLocalSource(File, String, ChatLanguageModel)}: Handles JPG/JPEG to PNG conversion and delegates to img2txtLocalPng.</li>
 *   <li>{@link #img2txtLocalPng(File, String, ChatLanguageModel)}: Encodes a PNG image to Base64 and sends it to the model with a prompt.</li>
 * </ul>
 */
public class Img2txt {

    /**
     * Converts a local image file (JPG, JPEG, or PNG) to text using a language model.
     * <p>
     * If the source file is a JPG or JPEG, it is converted to PNG format before processing.
     * The image and the provided prompt are sent to the specified ChatLanguageModel for text extraction or description.
     * </p>
     *
     * @param source the image file to process (JPG, JPEG, or PNG)
     * @param prompt the prompt to send along with the image to the language model
     * @param model the ChatLanguageModel to use for processing
     * @return the text output from the language model
     * @throws IOException if an I/O error occurs during file reading or writing
     */
    public static String img2txtLocalSource(@NotNull File source,
                                            @NotNull String prompt,
                                            @NotNull ChatLanguageModel model) throws IOException {

        //convert .jpg, .jpeg to png
        String sourceAbsolutePath = source.getAbsolutePath();
        if (sourceAbsolutePath.endsWith(".jpg") || sourceAbsolutePath.endsWith(".jpeg")){
            sourceAbsolutePath = sourceAbsolutePath.replace(".jpg",".png").replace(".jpeg",".png");
            File preexistent = new File(sourceAbsolutePath);
            if (preexistent.isFile()){
                return img2txtLocalPng(source,prompt,model);
            }

            BufferedImage image = ImageIO.read(source);

            source = new File(sourceAbsolutePath);
            ImageIO.write(image, "png", source);
        }

        return img2txtLocalPng(source,prompt,model);

    }

    /**
     * Converts a local PNG image file to text using a language model.
     * <p>
     * Reads the image, encodes it as Base64, and sends it along with the prompt to the specified ChatLanguageModel.
     * </p>
     *
     * @param source the PNG image file to process
     * @param prompt the prompt to send along with the image to the language model
     * @param model the ChatLanguageModel to use for processing
     * @return the text output from the language model
     * @throws IOException if an I/O error occurs during file reading
     */
    public static String img2txtLocalPng(@NotNull File source,
                                         @NotNull String prompt,
                                         @NotNull ChatLanguageModel model) throws IOException {
        //initialize the ImageContent
        byte[] imageBytes = Files.readAllBytes(Paths.get(source.getAbsolutePath()));
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        ImageContent imageContent = ImageContent.from(base64Data, "image/png");

        //initialize the UserMessage
        UserMessage userMessage = UserMessage.from(new TextContent(prompt),imageContent);

        //get output
        ChatResponse chatResponse = model.chat(userMessage);
        return chatResponse.aiMessage().text();
    }

}
