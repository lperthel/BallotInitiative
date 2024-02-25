package ballotInitiative;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CropGUI extends JFrame {
    private CropCallback callback;
    private BufferedImage originalImage;
    private BufferedImage displayedImage; // Scaled image for display
    private JLabel imageLabel;
    private final float scale = 0.25f; // Scale factor for the displayed image

    public CropGUI(BufferedImage image, CropCallback callback) {
        this.originalImage = image;
        this.callback = callback;
        // Scale the original image for display
        this.displayedImage = scaleImage(image, scale);
        initGUI();
    }

    private BufferedImage scaleImage(BufferedImage originalImage, float scale) {
        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return scaledImage;
    }

    private void initGUI() {
        setTitle("PNG Cropper");

        imageLabel = new JLabel(new ImageIcon(displayedImage));
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setSize(new Dimension(displayedImage.getWidth(), displayedImage.getHeight()));
        setLocationRelativeTo(null);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point startPoint;
            private Rectangle cropArea;

            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                cropArea = new Rectangle();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                cropArea.setBounds(
                        Math.min(startPoint.x, e.getX()),
                        Math.min(startPoint.y, e.getY()),
                        Math.abs(startPoint.x - e.getX()),
                        Math.abs(startPoint.y - e.getY()));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    if (cropArea.width > 0 && cropArea.height > 0) {
                        // Adjust the crop area to match the original image size
                        Rectangle adjustedCropArea = new Rectangle(
                                (int) (cropArea.x / scale),
                                (int) (cropArea.y / scale),
                                (int) (cropArea.width / scale),
                                (int) (cropArea.height / scale));

                        // Crop the original image based on the adjusted crop area
                        BufferedImage croppedImage = originalImage.getSubimage(
                                adjustedCropArea.x,
                                adjustedCropArea.y,
                                adjustedCropArea.width,
                                adjustedCropArea.height);

                        // Scale the cropped image for preview
                        BufferedImage previewImage = scaleImage(croppedImage, scale);

                        // Create a modal dialog with the scaled cropped image for preview
                        JDialog previewDialog = new JDialog(CropGUI.this, "Crop Preview", true);
                        previewDialog.setLayout(new BorderLayout());
                        previewDialog.add(new JLabel(new ImageIcon(previewImage)), BorderLayout.CENTER);

                        // Create a panel for buttons
                        JPanel buttonPanel = new JPanel();
                        JButton proceedButton = new JButton("Proceed");
                        JButton cancelButton = new JButton("Cancel");
                        buttonPanel.add(proceedButton);
                        buttonPanel.add(cancelButton);
                        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

                        // Add action listeners for the buttons
                        proceedButton.addActionListener(event -> {
                            previewDialog.dispose();
                            if (callback != null) {
                                callback.onCrop(adjustedCropArea);
                            }
                        });

                        cancelButton.addActionListener(event -> {
                            previewDialog.dispose();
                            imageLabel.setIcon(new ImageIcon(displayedImage)); // Reset to scaled image
                        });

                        // Show the dialog
                        previewDialog.pack();
                        previewDialog.setLocationRelativeTo(CropGUI.this);
                        previewDialog.setVisible(true);
                    } else {
                        System.out.println("Invalid crop area");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Error during cropping: " + ex.getMessage());
                }
            }
        };

        imageLabel.addMouseListener(mouseAdapter);
        imageLabel.addMouseMotionListener(mouseAdapter);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
