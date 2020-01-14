package client;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import server.User;

public class Client {

    private JFrame frame;
    private JTextArea textArea;

    private JTextField textField;

    private JTextField txt_port;
    private JTextField txt_hostIp;
    private JTextField txt_name;

    private JButton start;
    private JButton stop;
    private JButton send;
    private JButton sendFile;

    private JRadioButton groupchat;//群聊按钮
    private JRadioButton privatechat;//私聊按钮
    private ButtonGroup buttongroup;//按钮组
    private JPanel buttonPanel;
    private JPanel northPanel;
    private JPanel southPanel;

    private JScrollPane rightScroll;
    private JScrollPane leftScroll;//滚动窗格
    private JSplitPane centerSplit;


    private JList<String> userList;
    private DefaultListModel<String> listModel;

    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;// 负责接收消息的线程
    private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户，可以和他们私聊

    static FileSystemView fsv=FileSystemView.getFileSystemView();
    static File com=fsv.getHomeDirectory();

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        textArea = new JTextArea();
        textArea.setEditable(false);

        textField = new JTextField();
        txt_port = new JTextField("12345");
        txt_hostIp = new JTextField("192.168.123.68");
        Random rand = new Random();


        txt_name = new JTextField("用户" + rand.nextInt(100));
        start = new JButton("连接");
        stop = new JButton("断开");
        send = new JButton("发送");
        sendFile = new JButton("发送文件");
        listModel = new DefaultListModel<String>();
        userList = new JList<String>(listModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 7));
        northPanel.add(new JLabel("端口"));
        northPanel.add(txt_port);
        northPanel.add(new JLabel("服务器IP"));
        northPanel.add(txt_hostIp);
        northPanel.add(new JLabel("昵称"));
        northPanel.add(txt_name);
        northPanel.add(start);
        northPanel.add(stop);
        northPanel.setBorder(new TitledBorder("连接信息"));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder("消息显示区"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("在线用户"));

        groupchat = new JRadioButton("群聊");
        privatechat = new JRadioButton("私聊");

        privatechat.setSelected(true);
        buttongroup = new ButtonGroup();
        buttongroup.add(groupchat);
        buttongroup.add(privatechat);
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(groupchat);
        buttonPanel.add(privatechat);

        southPanel = new JPanel(new BorderLayout());
        JPanel inSouthPanel= new JPanel(new BorderLayout());
        inSouthPanel.add(send, "West");
        inSouthPanel.add(sendFile, "East");
        southPanel.add(buttonPanel, "North");
        southPanel.add(textField, "Center");
        southPanel.add(inSouthPanel,"East");
        southPanel.setBorder(new TitledBorder("写消息"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(100);

        frame = new JFrame("客户机");

        frame.setLayout(new BorderLayout());

        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        frame.setVisible(true);

        // 单击发送按钮事件
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        //单击发送文件按钮事件
        sendFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });

        // 单击连接按钮事件
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port;
                if (isConnected) {
                    JOptionPane.showMessageDialog(frame, "已处于连接上状态，不要重复连接!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    try {
                        port = Integer.parseInt(txt_port.getText().trim());
                    } catch (NumberFormatException e2) {
                        throw new Exception("端口号不符合要求!端口为整数!");
                    }
                    String hostIp = txt_hostIp.getText().trim();
                    String name = txt_name.getText().trim();
                    if (name.equals("") || hostIp.equals("")) {
                        throw new Exception("昵称、服务器IP不能为空!");
                    }
                    boolean flag = connectServer(port, hostIp, name);
                    if (flag == false) {
                        throw new Exception("与服务器连接失败!");
                    }
                    frame.setTitle(name);
                    JOptionPane.showMessageDialog(frame, "成功连接!");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // 单击断开按钮时事件
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    boolean flag = closeConnection();// 断开连接
                    if (flag == false) {
                        throw new Exception("断开连接发生异常！");
                    }
                    JOptionPane.showMessageDialog(frame, "成功断开!");
                    listModel.removeAllElements();
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    closeConnection();// 关闭连接
                }
                System.exit(0);// 退出程序
            }
        });


    }

    // 执行发送
    public void send() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = textField.getText().trim();
        String selectUser = "所有人";

        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (groupchat.isSelected()) {

            sendMessage("TOALL@" + frame.getTitle() + "@" + selectUser + "@" + message);

            //	System.out.println("群聊发送");
            textField.setText("");
        }
        if (privatechat.isSelected()) {

            selectUser = (String) userList.getSelectedValue();
            if (selectUser == null) {
                JOptionPane.showMessageDialog(frame, "请选择想私聊的用户!");
                return;
            }
            sendMessage("TOONE@" + frame.getTitle() + "@" + selectUser + "@" + message);
            String t = "我@" + selectUser + "说:" + message + "\r\n";

            textArea.append(t);
            //textArea.setForeground(Color.BLUE);
            textField.setText("");


        }

    }

    // 执行发送
    public void sendFile() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 创建一个默认的文件选择器
        JFileChooser fileChooser = new JFileChooser();
        // 设置默认显示的文件夹
        fileChooser.setCurrentDirectory(new File(String.valueOf(com)));
        // 添加可用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名）
//        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("(txt)", "txt"));
        // 设置默认使用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名 可变参数）
        fileChooser.setFileFilter(new FileNameExtensionFilter("(txt)", "txt"));
        // 打开文件选择框（线程将被堵塞，知道选择框被关闭）
        int result = fileChooser.showOpenDialog(frame);  // 对话框将会尽量显示在靠近 parent 的中心
        // 点击确定
        if(result == JFileChooser.APPROVE_OPTION) {
            // 获取路径
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            ClientFileThread.outFileToServer(path);
        }

    }

    //连接服务器
    public boolean connectServer(int port, String hostIp, String name) {
        // 连接服务器
        try {
            socket = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 发送客户端用户基本信息(用户名和ip地址)
            sendMessage(name + "@" + socket.getLocalAddress().toString());
            // 开启接收消息的线程
            messageThread = new MessageThread(reader, textArea);
            messageThread.start();
            isConnected = true;// 已经连接上了

            ClientFileThread fileThread = new ClientFileThread(name,hostIp,frame, writer,frame);
            fileThread.start();

            return true;

        } catch (Exception e) {
            textArea.append("与端口号为：" + port + "    IP地址为：" + hostIp
                    + "   的服务器连接失败!" + "\r\n");
            isConnected = false;// 未连接上
            return false;
        }
    }


    //发送消息
    public void sendMessage(String message) {

        writer.println(message);
        writer.flush();

    }

    //客户端主动关闭
    @SuppressWarnings("deprecation")
    public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// 发送断开连接命令给服务器
            messageThread.stop();// 停止接受消息线程
            // 释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isConnected = true;
            return false;
        }
    }


    // 不断接收消息的线程
    class MessageThread extends Thread {
        private BufferedReader reader;
        private JTextArea textArea;
        String username = textField.getName();

        // 接收消息线程的构造方法
        public MessageThread(BufferedReader reader, JTextArea textArea) {
            this.reader = reader;
            this.textArea = textArea;
        }

        // 被动的关闭连接
        public synchronized void closeCon() throws Exception {
            // 清空用户列表
            listModel.removeAllElements();
            // 被动的关闭连接释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;// 修改状态为断开
        }

        public void run() {
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();
                    StringTokenizer st = new StringTokenizer(message, "/@");

                    String parts = st.nextToken();//命令
                    switch (parts) {
                        case "CLOSE": {
                            textArea.append("服务器已关闭!\r\n");
                            closeCon();// 被动的关闭连接
                            return;// 结束线程
                        }
                        case "USERLIST": {


                            int size = Integer.parseInt(st.nextToken());
                            String username = null;
                            String userIp = null;

                            for (int i = 0; i < size; i++) {
                                username = st.nextToken();
                                userIp = st.nextToken();
                                User user = new User(username, userIp);
                                onLineUsers.put(username, user);
                                listModel.addElement(username);
                            }
                            break;
                        }
                        case "ADD": {
                            String username = "";
                            String userIp = "";
                            if ((username = st.nextToken()) != null
                                    && (userIp = st.nextToken()) != null) {

                                User user = new User(username, userIp);
                                onLineUsers.put(username, user);
                                listModel.addElement(username);
                                textArea.append("系统提示：" + username + "上线!\r\n");
                            }

                            break;
                        }
                        case "DELETE": {
                            String username = st.nextToken();
                            //	System.out.println(username+"下线");
                            User user = (User) onLineUsers.get(username);
                            onLineUsers.remove(user);
                            listModel.removeElement(username);
                            textArea.append("系统提示:" + username + "下线!\r\n");
                            userList.setModel(listModel);
                            break;
                        }
                        case "TOALL": {
                            textArea.append(st.nextToken() + "\r\n");
                            //System.out.println("群聊");
                            break;
                        }
                        case "TOONE": {
                            textArea.append(st.nextToken() + "\r\n");
                            //System.out.println("私聊");
                            //textArea.setForeground(Color.BLUE);//私聊的消息为蓝色
                            break;
                        }
                        default:
                            textArea.append(message + "\r\n");
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
