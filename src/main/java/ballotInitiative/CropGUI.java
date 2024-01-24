package ballotInitiative;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
    private JLabel imageLabel;

    public CropGUI(BufferedImage image, CropCallback callback) {
        this.originalImage = image;
        this.callback = callback;
        initGUI();
    }

    private void initGUI() {
        setTitle("PDF Cropper");

        imageLabel = new JLabel(new ImageIcon(originalImage));
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));
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
                        BufferedImage croppedImage = originalImage.getSubimage(
                                cropArea.x,
                                cropArea.y,
                                cropArea.width,
                                cropArea.height);

                        // Create a modal dialog with the cropped image
                        JDialog previewDialog = new JDialog(CropGUI.this, "Crop Preview", true);
                        previewDialog.setLayout(new BorderLayout());
                        previewDialog.add(new JLabel(new ImageIcon(croppedImage)), BorderLayout.CENTER);

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
                                callback.onCrop(cropArea);
                            }
                        });

                        cancelButton.addActionListener(event -> {
                            previewDialog.dispose();
                            imageLabel.setIcon(new ImageIcon(originalImage)); // Reset to original image
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
