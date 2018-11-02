package main;
import java.net.*;
import java.io.*;
public class Try {
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

  static class Handler implements Runnable{
    private Socket socket;
    public Handler(Socket socket){
        this.socket=socket;
    }
    
    public void run(){
      OutputStream clientOutput = null;
      InputStream clientInput = null;
      Socket proxySocket = null;
      InputStream proxyInput = null;
      OutputStream proxyOutput = null;
      String buffer = null;
      PrintWriter outPrintWriter_Web = null;//���writer��������վ��������
      BufferedReader proxyInput_buffer;//�������������������վ���͵�����
      String result;
      PrintWriter outPrintWriter_client;//���writer�����������д������
      try {
          clientInput = socket.getInputStream(); //�������������ȡ�����������
          clientOutput = socket.getOutputStream();
          outPrintWriter_client=new PrintWriter(clientOutput);
          String line;
          String host = "";
          BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
          buffer = reader.readLine(); // ��ȡ��һ��
          System.out.println(buffer);
          result = findHostandPort(buffer,reader);
          
          String[] hostTemp = result.split(":");
          host = hostTemp[0];//�õ�host
          int port = 80;
          if (hostTemp.length > 1) {
              port = Integer.valueOf(hostTemp[1]);
          }
          System.out.printf("host: %s port:%d\n",host, port);
          
          if(host!="") {
            System.out.printf("���ӵ�Ŀ�������");
            //���ӵ�Ŀ�������
            proxySocket = new Socket(host, port); //����һ�����׽��֣����Ұ�host��port�˿�����
            if(proxySocket != null) {
              //proxyOutput = proxySocket.getOutputStream();
              proxyInput = proxySocket.getInputStream();//��ȡ��վ���ص���Ӧ
              proxyInput_buffer = new BufferedReader(new InputStreamReader(proxyInput));
              outPrintWriter_Web = new PrintWriter(proxySocket.getOutputStream());//׼��������վ��������
            }
            //
            sendRequestToInternet(buffer,reader,outPrintWriter_Web);
            byte[] bytes=new byte[2048];
            int length=0;
            while(true){
              if((length=proxyInput.read(bytes))>0){
                clientOutput.write(bytes,0,length);
                  String show_response=new String(bytes,0,bytes.length);
                  System.out.println("���������ص���Ϣ��:\n---\n"+show_response+"\n---");
                  //write_cache(bytes,0,length);
                  //write_cache("\r\n".getBytes(),0,2);
                  continue;
              }
              break;
          }

          outPrintWriter_client.write("\r\n");
          outPrintWriter_client.flush();
          /*
          while (null != (line = reader.readLine())) {
            System.out.printf("line: %s\n",line);
            //headStr.append(line + "\r\n");
            if(line.length()!=0 && line.contains("Host")) {//ֻ�г���Host�Ž����и�
              String[] temp = line.split(" ");
              host = temp[1];
            }
            if(line.length()==0){
              break;
            }
            if(line.contains("CONNECT")||line.contains("google")||line.contains("c.gj.qq.com")){
              System.out.println("����"+line+"�ѱ�����");
              return ;//�˳�run()���������߳̾��Զ�����
            }
            System.out.printf("host_line: %s\n",host);
         }
          */
          
          

          /*
          //StringBuilder headStr = new StringBuilder();
          if(buffer.length()!=0 && buffer.contains("host")) {//ֻ�г���Host�Ž����и�
                String[] temp = buffer.split(" ");
                host = temp[1];
           }*/
          //����hostͷ������Ŀ���������host��port
          
                      
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
    
   
 }
  
  /**
   * ���ݴӷ������˻�ȡ������������host��port
   * @param buffer
   * @param reader
   * @return
   * @throws IOException
   */
  static String findHostandPort(String buffer,BufferedReader reader) throws IOException {
    String host = "";
    while (!buffer.equals("")) {
      System.out.printf("line: %s\n",buffer);
      //headStr.append(line + "\r\n");
      if(buffer.length()!=0 && buffer.contains("Host")) {//ֻ�г���Host�Ž����и�
        String[] temp = buffer.split(" ");
        host = temp[1];
      }
      System.out.printf("host_line: %s\n",host);
      buffer=reader.readLine();
   }
    return host;
  }
  
  /**
   * ����������Ѷ�����������web������
   * @param buffer
   * @param reader
   * @param outPrintWriter_Web
   * @throws IOException
   */
  static void sendRequestToInternet(String buffer,BufferedReader reader,PrintWriter outPrintWriter_Web) throws IOException {
    while(!buffer.equals("")){
        buffer+="\r\n";
        outPrintWriter_Web.write(buffer);
        System.out.print("��������:"+buffer+"\n");
        buffer=reader.readLine();
    }
    outPrintWriter_Web.write("\r\n");
    outPrintWriter_Web.flush();
}
}