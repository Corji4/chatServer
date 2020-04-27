package main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MainFrame extends JFrame {

    private static final String FRAME_TITLE = "Сервер";

    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;

    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;

    private static final int MEDIUM_GAP = 10;

    private static final int IN_PORT = 8080;
    private static ArrayList<Integer> outPorts = new ArrayList<Integer>(0);
    private static ArrayList<Address> allAddresses = new ArrayList<Address>(0);

    private final String MY_ADDRESS = InetAddress.getLocalHost().getHostAddress();

    private final JTextArea textAreaIncoming;

    private String senderName = null;
    private String message = null;
    private String photoName = null;

    private boolean successfulAuthorizationOrRegistration;

    private ArrayList<User> users = new ArrayList<>(0);

    private int deleteNumber;

    public MainFrame() throws UnknownHostException {
        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);
        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0);
        textAreaIncoming.setText("Адресс сервера: " + MY_ADDRESS + ":" + IN_PORT + "\n");
        textAreaIncoming.setEnabled(false);
        textAreaIncoming.setSelectedTextColor(Color.BLACK);
        textAreaIncoming.setDisabledTextColor(Color.BLACK);
        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);
        final JPanel messagePanel = new JPanel();
        // Компановка элементов фрейма
        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(IN_PORT);
                    while (!Thread.interrupted()) {
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());
                        final String messageType = in.readUTF();
                        final String senderIp = in.readUTF();
                        final String senderPort = in.readUTF();
                        Address fullAddress = new Address(senderIp, Integer.parseInt(senderPort));
                        if (!(allAddresses.contains(fullAddress))) {
                            allAddresses.add(fullAddress);
                        }
                        switch (messageType.toUpperCase()) {
                            case "AUTHORIZATION": {
                                String senderLogin = in.readUTF();
                                String senderPassword = in.readUTF();
                                User someUser = new User(senderLogin, senderPassword);

                                Scanner usersFile = new Scanner(new File("src/users.txt"));
                                // пропускаем уже записанных
                                for (int i = 0; i < users.size(); i++) {
                                    String info = usersFile.nextLine();
                                }
                                while (usersFile.hasNext()) {
                                    String[] info = usersFile.nextLine().split(" ");
                                    users.add(new User(info[0], info[1]));
                                }
                                successfulAuthorizationOrRegistration = false;
                                for (User user : users) {
                                    if (user.equals(someUser)) {
                                        successfulAuthorizationOrRegistration = true;
                                        break;
                                    }
                                }
                                sendMessage("AUTHORIZATION", fullAddress);
                                break;
                            }
                            case "REGISTRATION": {
                                String senderLogin = in.readUTF();
                                String senderPassword = in.readUTF();
                                User someUser = new User(senderLogin, senderPassword);

                                Scanner usersFile = new Scanner(new File("src/users.txt"));
                                // пропускаем уже записанных
                                for (int i = 0; i < users.size(); i++) {
                                    String info = usersFile.nextLine();
                                }
                                while (usersFile.hasNext()) {
                                    String[] info = usersFile.nextLine().split(" ");
                                    users.add(new User(info[0], info[1]));
                                }
                                usersFile.close();
                                successfulAuthorizationOrRegistration = true;
                                for (User user : users) {
                                    if (user.equals(someUser) ||
                                            (user.getLogin().equals(senderLogin) && !user.getPassword().equals(senderPassword))) {
                                        successfulAuthorizationOrRegistration = false;
                                        break;
                                    }
                                }
                                FileWriter addUser = new FileWriter(new File("src/users.txt"), true);
                                addUser.write("\n" + senderLogin + " " + senderPassword);
                                addUser.close();
                                sendMessage("REGISTRATION", fullAddress);
                                break;
                            }
                            case "SEND_ALL":
                                senderName = in.readUTF();
                                message = in.readUTF();
                                textAreaIncoming.append(senderName + " (" + fullAddress + "): \n" + message + "\n");
                                sendMessage("SEND_ALL");
                                break;
                            case "GET_FILE_LIST":
                                sendMessage("GET_FILE_LIST", fullAddress);
                                break;
                            case "GET_PHOTO_BY_NAME":
                                photoName = in.readUTF();
                                sendMessage("GET_PHOTO_BY_NAME", fullAddress);
                                break;
                            case "NEW_PHOTO":
                                String fileName = in.readUTF();
                                byte[] byteArray;
                                File photo = new File("src/images/" + fileName);
                                if (!photo.createNewFile()) {
                                    continue;
                                }
                                FileOutputStream fos = new FileOutputStream(photo);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);
                                byteArray = in.readAllBytes();
                                bos.write(byteArray, 0, byteArray.length);
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void sendMessage(String type, Address... addresses) {
        switch (type.toUpperCase()) {
            case "AUTHORIZATION":
                try {
                    Socket socket = new Socket(addresses[0].getIP(), addresses[0].getPort());
                    final DataOutputStream out =
                            new DataOutputStream(socket.getOutputStream());

                    out.writeUTF("AUTHORIZATION");
                    out.writeBoolean(successfulAuthorizationOrRegistration);
                    socket.close();
                } catch (Exception e) {

                }
                break;
            case "REGISTRATION":
                try {
                    Socket socket = new Socket(addresses[0].getIP(), addresses[0].getPort());
                    final DataOutputStream out =
                            new DataOutputStream(socket.getOutputStream());

                    out.writeUTF("REGISTRATION");
                    out.writeBoolean(successfulAuthorizationOrRegistration);
                    socket.close();
                } catch (Exception e) {

                }
                break;
            case "SEND_ALL":
                for (int i = 0; i < allAddresses.size(); i++) {
                    deleteNumber = i;
                    try {
                        Socket socket = new Socket(allAddresses.get(i).getIP(), allAddresses.get(i).getPort());
                        final DataOutputStream out =
                                new DataOutputStream(socket.getOutputStream());
                        // Записываем в поток
                        out.writeUTF("SEND_ALL");
                        out.writeUTF(senderName);
                        out.writeUTF(message);
                        // Закрываем сокет
                        socket.close();
                    } catch (Exception e) {
                        //DELETE address
                        allAddresses.removeIf(address -> (address.equals(allAddresses.get(deleteNumber))));
                        i--;
                    }
                }
                break;
            case "GET_FILE_LIST":
                File folder = new File("src/images/");
                File[] files = folder.listFiles();
                StringBuilder fileNames = new StringBuilder();
                for (File file : files) {
                    fileNames.append(file.getName()).append("\n");
                }
                try {
                    Socket socket = new Socket(addresses[0].getIP(), addresses[0].getPort());
                    final DataOutputStream out =
                            new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("GET_FILE_LIST");
                    out.writeUTF(fileNames.toString());
                    socket.close();
                } catch (Exception e) {

                }
                break;
            case "GET_PHOTO_BY_NAME":
                File photo = new File("src/images/" + photoName);
                try {
                    byte[] byteArray = new byte[(int) photo.length()];
                    FileInputStream in = new FileInputStream(photo);
                    BufferedInputStream bis = new BufferedInputStream(in);
                    bis.read(byteArray, 0, byteArray.length);

                    Socket socket = new Socket(addresses[0].getIP(), addresses[0].getPort());
                    final DataOutputStream out =
                            new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("GET_PHOTO_BY_NAME");
                    out.write(byteArray, 0, byteArray.length);
                    socket.close();
                } catch (Exception e) {

                }
                break;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainFrame frame;
                try {
                    frame = new MainFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setVisible(true);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
