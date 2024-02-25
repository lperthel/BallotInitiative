package ballotInitiative;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MainApplication {
    private final static String initiativeScansInputLocation = "C:\\Users\\lpert\\git\\DC Civic Tech\\Ballot Initiative\\Ballot Inititive Scans\\test.pdf";
    private final static String initiativeScansOutputLocation = "C:\\Users\\lpert\\git\\DC Civic Tech\\Ballot Initiative\\Ballot Inititive Scans\\";

    public static void main(String[] args) {
        try {
            // Convert the pdf to png
            PDFProcessor.convertPDFToPNG(initiativeScansInputLocation, initiativeScansOutputLocation);
            // Create and display the CropGUI
            File firstPageFile = new File(initiativeScansOutputLocation + "page_0.png");
            if (!firstPageFile.exists()) {
                System.err.println("First page PNG file not found.");
                return;
            }
            BufferedImage firstPageImage = ImageIO.read(firstPageFile);

            new CropGUI(firstPageImage, cropArea -> {
                try {
                    // Process the PDF using the selected crop area
                    Rectangle2D rect = new Rectangle2D.Double(cropArea.x, cropArea.y, cropArea.width, cropArea.height);
                    PDFProcessor pdfProcessor = new PDFProcessor(rect);
                    pdfProcessor.cropAndSaveAsPNG(initiativeScansInputLocation, initiativeScansOutputLocation);
                    System.out.println("Done!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // TODO: Make this run after the callback is complete.
            recombineCroppedImages();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void recombineCroppedImages() {
        try {
            // Assume images are cropped and saved already, and their paths are stored in
            // imagePaths array
            String[] imagePaths = {
                    "path/to/cropped_image_1.png",
                    "path/to/cropped_image_2.png",
                    // Add more paths as needed
            };

            String outputPath = "path/to/combined_output.pdf";
            PDFProcessor.combineImagesIntoPDF(imagePaths, outputPath);

            System.out.println("Combined PDF created successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
