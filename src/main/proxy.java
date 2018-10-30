package main;
import java.net.*;
import java.util.concurrent.CountDownLatch;
import java.io.*;
import java.nio.*;
public class proxy {
  public static final int ProxyPort = 10240; 
  private static ServerSocket serverSocket = null;
  public static void main(String[] args) throws IOException {
    System.out.println("������������ڳ�ʼ��");
    serverSocket = new ServerSocket(ProxyPort);    
    while(true){
      System.out.println("����socket�ɹ�");
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
            //��ȡHTTP����ͷ�����õ�HOST����ͷ��method
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
          //��ȡHTTP����ͷ�����õ�HOST����ͷ��method
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
          //��ȡHTTP����ͷ�����õ�HOST����ͷ��method
          while (null != (line = reader.readLine())) {
              System.out.printf("line: %s\n",line);
              headStr.append(line + "\r\n");
              if(line.length()!=0 && line.contains("Host")) {//ֻ�г���Host�Ž����и�
                String[] temp = line.split(" ");
                host = temp[1];
              }
              if(line.length()==0){
                break;
              }
              System.out.printf("host_line: %s\n",host);
          }
          String type = headStr.substring(0, headStr.indexOf(" "));
          //����hostͷ������Ŀ���������host��port
          /*String[] hostTemp = host.split(":");
          host = hostTemp[0];//�õ�host
          System.out.printf("host: %s\n",host);
          int port = 80;
          if (hostTemp.length > 1) {
              port = Integer.valueOf(hostTemp[1]);
          }*/
          //���ӵ�Ŀ�������
          int port = 80;
          proxySocket = new Socket(host, port); //����һ��
          proxyOutput = proxySocket.getOutputStream();
          //����HTTP method���ж���https����http����
          if ("CONNECT".equalsIgnoreCase(type)) {//https�Ƚ������
              clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
              clientOutput.flush();
          } else {//httpֱ�ӽ�����ͷת��
              proxyOutput.write(headStr.toString().getBytes());
          }
          //�¿��߳�ת���ͻ���������Ŀ�������()
          new ProxyHandleThread(clientInput, proxyOutput).start();
          //ת��Ŀ���������Ӧ���ͻ���(������ת�������������������)
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
  //˼·����ʼ��socket��Ȼ�󴴽�socket
  //************************************ 
  // Method:    InitSocket 
  // FullName:  InitSocket 
  // Access:    public  
  // Returns:   BOOL 
  // Qualifier: ��ʼ���׽��� 
  //************************************
  
  
  //************************************ 
  // Method:    ProxyThread 
  // FullName:  ProxyThread 
  // Access:    public  
  // Returns:   unsigned int __stdcall 
  // Qualifier: �߳�ִ�к��� 
  // Parameter: LPVOID lpParameter
  //************************************ 
   
  //************************************ 
  // Method:    ParseHttpHead 
  // FullName:  ParseHttpHead 
  // Access:    public  
  // Returns:   void 
  // Qualifier: ���� TCP �����е� HTTP ͷ�� 
  // Parameter: char * buffer 
  // Parameter: HttpHeader * httpHeader 
  //************************************
  
  //************************************ 
  // Method:    ConnectToServer 
  // FullName:  ConnectToServer 
  // Access:    public  
  // Returns:   BOOL 
  // Qualifier: ������������Ŀ��������׽��֣������� 
  // Parameter: SOCKET * serverSocket // Parameter: char * host 

  
}
