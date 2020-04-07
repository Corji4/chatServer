import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private static final String FRAME_TITLE = "Сервер";

    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;

    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;

    private static final int MEDIUM_GAP = 10;

    private static final int IN_PORT = 8080;
    private static ArrayList<Integer> OUT_PORTS = new ArrayList<Integer>(0);

    private final String MY_ADDRESS = InetAddress.getLocalHost().getHostAddress();

    private final JTextArea textAreaIncoming;

    private String senderName = null;
    private String message = null;

    public MainFrame() throws UnknownHostException {
        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);
        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0);
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
                        // Читаем из потока
                        senderName = in.readUTF();
                        message = in.readUTF();
                        Integer somePort = Integer.parseInt(in.readUTF());
                        if (!(OUT_PORTS.contains(somePort))) {
                            OUT_PORTS.add(somePort);
                        }
                        // Закрываем соединение
                        socket.close();
                        final String address = ((InetSocketAddress) socket
                                .getRemoteSocketAddress())
                                .getAddress()
                                .getHostAddress();
                        // Выводим сообщение в текстовую область
                        textAreaIncoming.append(senderName + " (" + address + "): " + message + "\n");
                        sendMessage();
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
        try {
            // Получаем необходимые параметры
            final String destinationAddress = MY_ADDRESS;
            // Создадим сокет для соединения
            for(Integer port: OUT_PORTS) {
                final Socket socket = new Socket(destinationAddress, port);
                // Открываем поток вывода данных
                final DataOutputStream out =
                        new DataOutputStream(socket.getOutputStream());
                // Записываем в поток
                out.writeUTF(senderName);
                out.writeUTF(message);
                // Закрываем сокет
                socket.close();
            }
            // Очищаем текстовую область ввода сообщения
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение: узел-адресат не найден",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
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
