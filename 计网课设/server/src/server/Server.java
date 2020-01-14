package server;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class Server extends JFrame {

    private static final long serialVersionUID = 1L;
    private ServerSocket serveSocket;
    private ServerThread serverThread;
    private ArrayList<ClientThread> clients;

    private JFrame frame;
    private JTextArea txt1;
    private JTextField txt_message;
    private JTextField txt_port;
    private JButton start;
    private JButton send;
    private JButton stop;
    private JPanel northPanle;
    private JPanel southPanle;
    private JScrollPane leftPanle;
    private JScrollPane rightPanle;
    private JSplitPane centerSplit;

    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private boolean isStart = false;
    private ServerFileThread serverFileThread;
    public static void main(String[] args) {
        new Server();

    }

    //构造函数
    public Server() {
        frame = new JFrame("服务器");
        txt1 = new JTextArea();
        txt_message = new JTextField(30);
        txt_port = new JTextField("12345");
        start = new JButton("监听此端口");
        stop = new JButton("停止服务器");
        send = new JButton("发送");
        listModel = new DefaultListModel<String>();
        userList = new JList<String>(listModel);
        //userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        southPanle = new JPanel(new BorderLayout());
        southPanle.setBorder(new TitledBorder("写消息"));
        southPanle.add(txt_message, BorderLayout.CENTER);
        southPanle.add(send, BorderLayout.EAST);

        leftPanle = new JScrollPane(userList);
        leftPanle.setBorder(new TitledBorder("在线用户"));

        rightPanle = new JScrollPane(txt1);
        rightPanle.setBorder(new TitledBorder("消息显示区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanle, rightPanle);
        centerSplit.setDividerLocation(100);

        northPanle = new JPanel();
        northPanle.setLayout(new GridLayout(1, 6));
        northPanle.add(start);
        northPanle.add(txt_port);
        northPanle.add(stop);

        frame.setLayout(new BorderLayout());
        frame.add(northPanle, BorderLayout.NORTH);
        frame.add(centerSplit, BorderLayout.CENTER);
        frame.add(southPanle, BorderLayout.SOUTH);
        frame.setSize(600, 400);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @SuppressWarnings("unused")
            public void windowclosing(WindowEvent e) {
                if (isStart) {
                    closeServer();//关闭服务器
                }
                System.exit(0);//退出程序
            }
        });
        @SuppressWarnings("unused")
        int port = Integer.parseInt(txt_port.getText());
//	监听端口事件，即启动服务器
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器已处于启动状态", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int port;
                try {
                    try {
                        port = Integer.parseInt(txt_port.getText());
                    } catch (Exception e1) {
                        throw new Exception("端口号 为正整数！");
                    }
                    if (port <= 0) {
                        throw new Exception("端口号 为正整数！");

                    }

                    serverFileThread = new ServerFileThread();//启动文件线程
                    serverFileThread.start();

                    serverStart(port);
                    txt1.append("服务器已启动,端口:" + port + ",正在等待客户端连接...\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功启动");
                    start.setEnabled(false);
                    txt_port.setEnabled(false);
                    stop.setEnabled(true);
                } catch (Exception ee) {
                    JOptionPane.showMessageDialog(frame, ee.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }

            }

        });
        // 单击停止服务器按钮时事件
        stop.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
                if (!isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器还未启动！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    closeServer();
                    serverFileThread.stop();
                    start.setEnabled(true);
                    txt_port.setEnabled(true);
                    stop.setEnabled(false);
                    txt1.append("服务器成功停止!\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功停止！");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, "停止服务器发生异常！", "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // 单击发送按钮时事件
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

    }


    //启动服务器
    public void serverStart(int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serveSocket = new ServerSocket(port);
            serverThread = new ServerThread(serveSocket);
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            isStart = false;
            throw new BindException("端口号已被占用，换一个");
        } catch (Exception e) {
            e.printStackTrace();
            isStart = false;
            throw new BindException("启动服务器异常");
        }

    }

    // 关闭服务器
    @SuppressWarnings("deprecation")
    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// 停止服务器线程

            if(serverFileThread!=null) {
                serverFileThread.closeThread();
                serverFileThread.stop();
            }

            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // 释放资源
                clients.get(i).stop();// 停止此条为客户端服务的线程
                clients.get(i).reader.close();
                clients.get(i).writer.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serveSocket != null) {
                serveSocket.close();// 关闭服务器端连接
            }
            listModel.removeAllElements();// 清空用户列表
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
    }

    // 执行消息发送
    public void send() {
        if (!isStart) {
            JOptionPane.showMessageDialog(frame, "服务器未启动，不能发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clients.size() == 0) {
            JOptionPane.showMessageDialog(frame, "没有用户在线,不能发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = txt_message.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendServerMessage(message);// 群发服务器消息
        txt1.append("服务器提示：" + txt_message.getText() + "\r\n");//服务器说的话显示在服务器界面
        txt_message.setText(null);
    }

    // 把后台消息发送给各个客户端
    public void sendServerMessage(String message) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).getWriter().println("系统提示：" + message + "(群发)");//服务器获得的输出流发送给客户端界面
            clients.get(i).getWriter().flush();
        }
    }


    //每个连接到服务器的客户端，又有与之对应的一个线程来单独处理，收发消息
    class ClientThread extends Thread {

        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        private User user;

        public BufferedReader getReader() {
            return reader;

        }

        public PrintWriter getWriter() {
            return writer;

        }

        public User getUser() {
            return user;

        }

        //每个客户端对应一个客户端线程处理
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                //接收客户端的基本信息
                String line = reader.readLine();
                String[] st = line.split("@");

                user = new User(st[0], st[1]);
                //反馈连接成功消息
                writer.println("系统提示：" + user.getName() + user.getIp() + "与服务器连接成功!");

                //System.out.println(user.getName()+".."+user.getIp());
                //反馈当前用户信息
                if (clients.size() > 0) {
                    String temp = "";
                    for (int i = clients.size() - 1; i >= 0; i--) {
                        temp += (clients.get(i).getUser().getName() + "/" + clients.get(i).getUser().getIp()) + "@";

                    }

                    writer.println("USERLIST@" + clients.size() + "@" + temp);
                    writer.flush();

                }
                System.out.println(st[0] + ",服务器显示上线" + st[1]);
                //向所有在线用户发送该用户上线命令,即把新上线的用户添加在在线用户列表中
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println("ADD@" + user.getName() + "@" + user.getIp());
                    clients.get(i).getWriter().flush();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }

        @SuppressWarnings("deprecation")
        public void run() {//不断接受客户端的消息进行处理
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();//接收客户端消息
                    System.out.println(message);
                    if (message.equals("CLOSE"))// 下线命令
                    {
                        txt1.append(this.getUser().getName() + this.getUser().getIp() + "下线!\r\n");

                        // 断开连接释放资源
                        reader.close();
                        writer.close();
                        socket.close();

                        // 向所有在线用户发送该用户的下线命令
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            clients.get(i).getWriter().println("DELETE@" + user.getName());
                            clients.get(i).getWriter().flush();
                        }

                        listModel.removeElement(user.getName());// 更新在线列表
                        // 删除此条客户端服务线程
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                clients.remove(i);// 删除此用户的服务线程
                                temp.stop();// 停止这条服务线程
                                return;
                            }
                        }

                    } else {
                        dispatcherMessage(message);// 转发消息
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }

        private void dispatcherMessage(String message) {


            String[] parts = message.split("@");
            String string = parts[1] + "对" + parts[2] + "说:" + parts[3];
            if (parts[0].equals("TOALL")) {// 群发
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println("TOALL@" + string);
                    clients.get(i).getWriter().flush();
                    //System.out.println("群聊消息发送");
                }
                txt1.append(string + "\r\n");
            }
            if (parts[0].equals("TOONE")) {//私发
                for (int i = 0; i < clients.size(); i++) {
                    if (parts[2].equals((clients.get(i).getUser().getName()))) {
                        string = parts[1] + "对我说:" + parts[3];
                        clients.get(i).getWriter().println("TOONE@" + string);
                        clients.get(i).getWriter().flush();
                        //System.out.println("私聊消息发送");
                    }
                }
                txt1.append(parts[1] + "对" + parts[2] + "说:" + parts[3] + "\r\n");
            }
        }

    }

    class ServerThread extends Thread {
        private ServerSocket serverSocket;

        // 服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;

        }

        public void run() {
            while (true) {// 不停的等待客户端的链接
                try {
                    Socket socket = serverSocket.accept();
                    ClientThread client = new ClientThread(socket);
                    client.start();// 开启对此客户端服务的线程
                    clients.add(client);
                    listModel.addElement(client.getUser().getName());// 更新在线列表
                    txt1.append(client.getUser().getName() + client.getUser().getIp() + "上线!\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}