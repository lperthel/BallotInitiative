package ballotInitiative;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class MainApplication {
    private final static String initiativeScansInputLocation = "C:\\Users\\lpert\\git\\DC Civic Tech\\Ballot Initiative\\Ballot Inititive Scans\\test.pdf";
    private final static String initiativeScansOutputLocation = "C:\\Users\\lpert\\git\\DC Civic Tech\\Ballot Initiative\\Ballot Inititive Scans\\";

    public static void main(String[] args) {
        try {
            // Load the first page of the PDF
            BufferedImage firstPageImage = loadFirstPageImage(initiativeScansInputLocation);

            // Create and display the CropGUI
            CropGUI cropGUI = new CropGUI(firstPageImage, cropArea -> {
                try {
                    // Process the PDF using the selected crop area
                    PDFProcessor pdfProcessor = new PDFProcessor(cropArea);
                    // pdfProcessor.cropAndSavePDF(initiativeScansInputLocation,
                    // initiativeScansOutputLocation);
                    pdfProcessor.cropAndSaveAsPNG(initiativeScansInputLocation, initiativeScansOutputLocation);
                    System.out.println("Done!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage loadFirstPageImage(String path) throws IOException {
        BufferedImage image = null;
        try (PDDocument document = Loader.loadPDF(new File(path))) {
            PDFRenderer renderer = new PDFRenderer(document);
            // Assuming you want to render the first page (index 0)
            image = renderer.renderImage(0, 1.0f); // 1.0f is the scaling factor (1x)
        }
        return image;
    }
}
