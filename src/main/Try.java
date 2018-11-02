package main;
import java.net.*;
import java.io.*;
public class Try {
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
      PrintWriter outPrintWriter_Web = null;//这个writer用来向网站发送请求
      BufferedReader proxyInput_buffer;//这个缓冲用来缓存向网站发送的请求
      String result;
      PrintWriter outPrintWriter_client;//这个writer用来向浏览器写入数据
      try {
          clientInput = socket.getInputStream(); //创建从浏览器获取请求的输入流
          clientOutput = socket.getOutputStream();
          outPrintWriter_client=new PrintWriter(clientOutput);
          String line;
          String host = "";
          BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
          buffer = reader.readLine(); // 读取第一行
          System.out.println(buffer);
          result = findHostandPort(buffer,reader);
          
          String[] hostTemp = result.split(":");
          host = hostTemp[0];//拿到host
          int port = 80;
          if (hostTemp.length > 1) {
              port = Integer.valueOf(hostTemp[1]);
          }
          System.out.printf("host: %s port:%d\n",host, port);
          
          if(host!="") {
            System.out.printf("连接到目标服务器");
            //连接到目标服务器
            proxySocket = new Socket(host, port); //创建一个流套接字，并且把host跟port端口连接
            if(proxySocket != null) {
              //proxyOutput = proxySocket.getOutputStream();
              proxyInput = proxySocket.getInputStream();//获取网站返回的响应
              proxyInput_buffer = new BufferedReader(new InputStreamReader(proxyInput));
              outPrintWriter_Web = new PrintWriter(proxySocket.getOutputStream());//准备好向网站发送请求
            }
            //
            sendRequestToInternet(buffer,reader,outPrintWriter_Web);
            byte[] bytes=new byte[2048];
            int length=0;
            while(true){
              if((length=proxyInput.read(bytes))>0){
                clientOutput.write(bytes,0,length);
                  String show_response=new String(bytes,0,bytes.length);
                  System.out.println("服务器发回的消息是:\n---\n"+show_response+"\n---");
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
            if(line.length()!=0 && line.contains("Host")) {//只有出现Host才进行切割
              String[] temp = line.split(" ");
              host = temp[1];
            }
            if(line.length()==0){
              break;
            }
            if(line.contains("CONNECT")||line.contains("google")||line.contains("c.gj.qq.com")){
              System.out.println("请求"+line+"已被过滤");
              return ;//退出run()方法，该线程就自动结束
            }
            System.out.printf("host_line: %s\n",host);
         }
          */
          
          

          /*
          //StringBuilder headStr = new StringBuilder();
          if(buffer.length()!=0 && buffer.contains("host")) {//只有出现Host才进行切割
                String[] temp = buffer.split(" ");
                host = temp[1];
           }*/
          //根据host头解析出目标服务器的host和port
          
                      
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
    
   
 }
  
  /**
   * 根据从服务器端获取的流，解析出host和port
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
      if(buffer.length()!=0 && buffer.contains("Host")) {//只有出现Host才进行切割
        String[] temp = buffer.split(" ");
        host = temp[1];
      }
      System.out.printf("host_line: %s\n",host);
      buffer=reader.readLine();
   }
    return host;
  }
  
  /**
   * 代理服务器把读入的流传输给web服务器
   * @param buffer
   * @param reader
   * @param outPrintWriter_Web
   * @throws IOException
   */
  static void sendRequestToInternet(String buffer,BufferedReader reader,PrintWriter outPrintWriter_Web) throws IOException {
    while(!buffer.equals("")){
        buffer+="\r\n";
        outPrintWriter_Web.write(buffer);
        System.out.print("发送请求:"+buffer+"\n");
        buffer=reader.readLine();
    }
    outPrintWriter_Web.write("\r\n");
    outPrintWriter_Web.flush();
}
}