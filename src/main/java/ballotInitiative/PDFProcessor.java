package ballotInitiative;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class PDFProcessor {

    private Rectangle2D cropBox;

    public PDFProcessor(Rectangle2D cropBox) {
        this.cropBox = cropBox;
    }

    public void cropAndSaveAsPNG(String inputPath, String outputDir) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                BufferedImage croppedImage = bim.getSubimage(
                        (int) cropBox.getX(),
                        (int) cropBox.getY(),
                        (int) cropBox.getWidth(),
                        (int) cropBox.getHeight());

                File outputFile = new File(outputDir, "cropped_page_" + page + ".png");
                ImageIO.write(croppedImage, "png", outputFile);
            }
        }

    }

    public void cropAndSavePDF(String inputPath, String outputPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(inputPath));
                PDDocument newDocument = new PDDocument()) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                // Crop the image
                BufferedImage croppedImage = bim.getSubimage(
                        (int) cropBox.getX(),
                        (int) cropBox.getY(),
                        (int) cropBox.getWidth(),
                        (int) cropBox.getHeight());

                // Save each cropped image as a new PDF page
                PDPage newPage = new PDPage();
                newDocument.addPage(newPage);

                String individualPagePath = outputPath.replace(".pdf", "_" + page + ".pdf");
                try (OutputStream outputStream = new FileOutputStream(individualPagePath)) {
                    ImageIOUtil.writeImage(croppedImage, "png", outputStream, 300);
                }
            }

            newDocument.save(outputPath);
            newDocument.close();
        }
    }

}
