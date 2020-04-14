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
                        final InputStream in = socket.getInputStream();
                        FileOutputStream fOut = new FileOutputStream(new File("src/messageSettings"));
                        byte[] bt = new byte[1024];
                        while ((in.read(bt)) > 0) {
                            fOut.write(bt);
                        }
                        fOut.close();
                        Scanner fIn = new Scanner(new File("src/messageSettings"));
                        Address fullAddress = new Address(fIn.nextLine(), Integer.parseInt(fIn.nextLine()));
                        if (!(allAddresses.contains(fullAddress))) {
                            allAddresses.add(fullAddress);
                        }
                        String messageType = fIn.nextLine();
                        switch (messageType) {
                            case "SEND_ALL": {
                                senderName = fIn.nextLine();
                                message = "";
                                while (fIn.hasNext()) {
                                    message += "        │" + fIn.nextLine() + "\n";
                                }
                                textAreaIncoming.append(senderName + " (" + fullAddress + "): \n" + message + "\n");
                                sendMessage();
                            }
                            case "CHECK_CONNECTION": {
                                //NOTING
                            }
                            default: {
                            }
                        }
//                        if (messageType.equals("SEND_ALL")) {
//                            senderName = fIn.nextLine();
//                            message = "";
//                            while (fIn.hasNext()) {
//                                message += "        │" + fIn.nextLine() + "\n";
//                            }
//                            textAreaIncoming.append(senderName + " (" + fullAddress + "): \n" + message + "\n");
//                            sendMessage();
//                        } else if (messageType.equals("CHECK_CONNECTION")) {
//                            // NOTHING
//                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void sendMessage() {
        for (int i = 0; i < allAddresses.size(); i++) {
            deleteNumber = i;
            try {
                Socket socket = new Socket(allAddresses.get(i).getIP(), allAddresses.get(i).getPort());
                final DataOutputStream out =
                        new DataOutputStream(socket.getOutputStream());
                // Записываем в поток
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
