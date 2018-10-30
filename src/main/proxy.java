package main;
import java.net.*;
import java.util.concurrent.CountDownLatch;
import java.io.*;
import java.nio.*;
public class proxy {
  public static final int ProxyPort = 10240; 
  private static ServerSocket serverSocket = null;
  public static void main(String[] args) throws IOException {
    System.out.println("代理服务器正在初始化");
    serverSocket = new ServerSocket(ProxyPort);    
    while(true){
      System.out.println("创建socket成功");
      Socket socket = serverSocket.accept();
      Thread workThread=new Thread(new Handler(socket));
      workThread.start();
    }
    
    
  }
  
  static class SocketHandle extends Thread {

    private Socket socket;

    public SocketHandle(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        System.out.println("socketin");
        try {
            System.out.println("socketin");
            //读取HTTP请求头，并拿到HOST请求头和method
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            System.out.println(line);
        }catch (IOException e) {
            e.printStackTrace();
        } 
  }
    
  }
  
  static class Handler implements Runnable{
    private Socket socket;
    public Handler(Socket socket){
        this.socket=socket;
    }
    
    public void run(){
      /**
      try {
          System.out.println("socketin");
          //读取HTTP请求头，并拿到HOST请求头和method
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          String line = reader.readLine();
          System.out.println(line);
      }catch (IOException e) {
          e.printStackTrace();
      } **/
      OutputStream clientOutput = null;
      InputStream clientInput = null;
      Socket proxySocket = null;
      InputStream proxyInput = null;
      OutputStream proxyOutput = null;
      try {
          clientInput = socket.getInputStream();
          clientOutput = socket.getOutputStream();
          String line;
          String host = "";
          BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
          StringBuilder headStr = new StringBuilder();
          //读取HTTP请求头，并拿到HOST请求头和method
          while (null != (line = reader.readLine())) {
              System.out.printf("line: %s\n",line);
              headStr.append(line + "\r\n");
              if(line.length()!=0 && line.contains("Host")) {//只有出现Host才进行切割
                String[] temp = line.split(" ");
                host = temp[1];
              }
              if(line.length()==0){
                break;
              }
              System.out.printf("host_line: %s\n",host);
          }
          String type = headStr.substring(0, headStr.indexOf(" "));
          //根据host头解析出目标服务器的host和port
          /*String[] hostTemp = host.split(":");
          host = hostTemp[0];//拿到host
          System.out.printf("host: %s\n",host);
          int port = 80;
          if (hostTemp.length > 1) {
              port = Integer.valueOf(hostTemp[1]);
          }*/
          //连接到目标服务器
          int port = 80;
          proxySocket = new Socket(host, port); //创建一个
          proxyOutput = proxySocket.getOutputStream();
          //根据HTTP method来判断是https还是http请求
          if ("CONNECT".equalsIgnoreCase(type)) {//https先建立隧道
              clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
              clientOutput.flush();
          } else {//http直接将请求头转发
              proxyOutput.write(headStr.toString().getBytes());
          }
          //新开线程转发客户端请求至目标服务器()
          new ProxyHandleThread(clientInput, proxyOutput).start();
          //转发目标服务器响应至客户端(服务器转发给代理服务器的内容)
          proxyInput = proxySocket.getInputStream();
          while (true) {
              clientOutput.write(proxyInput.read());
          }
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          
          if (proxyInput != null) {
              try {
                  proxyOutput.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if (proxyOutput != null) {
              try {
                  proxyOutput.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if (proxySocket != null) {
              try {
                  proxySocket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if (clientInput != null) {
              try {
                  clientInput.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if (clientOutput != null) {
              try {
                  clientOutput.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if (socket != null) {
              try {
                  socket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }

  }
    }
    
  
  static class ProxyHandleThread extends Thread {

    private InputStream input;
    private OutputStream output;

    public ProxyHandleThread(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            while (true) {
                output.write(input.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  }
  //思路：初始化socket，然后创建socket
  //************************************ 
  // Method:    InitSocket 
  // FullName:  InitSocket 
  // Access:    public  
  // Returns:   BOOL 
  // Qualifier: 初始化套接字 
  //************************************
  
  
  //************************************ 
  // Method:    ProxyThread 
  // FullName:  ProxyThread 
  // Access:    public  
  // Returns:   unsigned int __stdcall 
  // Qualifier: 线程执行函数 
  // Parameter: LPVOID lpParameter
  //************************************ 
   
  //************************************ 
  // Method:    ParseHttpHead 
  // FullName:  ParseHttpHead 
  // Access:    public  
  // Returns:   void 
  // Qualifier: 解析 TCP 报文中的 HTTP 头部 
  // Parameter: char * buffer 
  // Parameter: HttpHeader * httpHeader 
  //************************************
  
  //************************************ 
  // Method:    ConnectToServer 
  // FullName:  ConnectToServer 
  // Access:    public  
  // Returns:   BOOL 
  // Qualifier: 根据主机创建目标服务器套接字，并连接 
  // Parameter: SOCKET * serverSocket // Parameter: char * host 

  
}
