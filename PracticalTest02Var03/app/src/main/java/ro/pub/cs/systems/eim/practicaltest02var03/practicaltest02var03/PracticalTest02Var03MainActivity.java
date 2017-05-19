package ro.pub.cs.systems.eim.practicaltest02var03.practicaltest02var03;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

import static java.lang.Integer.parseInt;

public class  PracticalTest02Var03MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText t_port;
    private EditText t_word;

    private TextView t_result;

    private Button b_Start;
    private Button b_Get;

    private ServerSocket serverSocket;

    HashMap<String, String> memory;

    public class CommunicationThread extends  Thread {

        final ServerThread servarThread;
        final Socket socket;

        public CommunicationThread(ServerThread serverThread, Socket socket) {
            this.servarThread = serverThread;
            this.socket = socket;
        }

        public void run() {
            if (socket == null) {
//                Log.e(1, "[COMMUNICATION THREAD] Socket is null!");
                return;
            }

            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                String word = input.readLine();

                if (memory.containsKey(word)) {
                    output.println(memory.get(word));
                    return;
                }

                HttpClient httpClient = new DefaultHttpClient();

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("word", word));

                String paramString = URLEncodedUtils.format(params, "utf-8");
                HttpGet httpGet = new HttpGet("http://services.aonaware.com/DictService/DictService.asmx/Define?" + paramString);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                memory.put(word, pageSourceCode);

                output.println(memory.get(word));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServerThread extends Thread {
        public ServerThread() {
        }

        public void run() {
            try {
                String port = t_port.getText().toString();
                Log.d("Heloo", port);
                serverSocket = new ServerSocket(parseInt(port));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    CommunicationThread communicationThread = new CommunicationThread(this, socket);
                    communicationThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class ClientThread extends Thread {

        final String address;
        final int port;
        final String word;

        public ClientThread(String address, int port, String word) {
            this.address = address;
            this.port = port;
            this.word = word;
        }

        public void run() {
            try {
                Log.d("Helo", "Creating socket!");
                Socket socket = new Socket(address, port);
                Log.d("Helo", "Created socket!");

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                output.println(input);
                output.flush();

                String line;
                String text = "";

                while ((line = input.readLine()) != null) {
                    text += line;
                }

                t_result.setText(text);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_var03_main);

        b_Start = (Button) findViewById(R.id.b_start);
        b_Get = (Button) findViewById(R.id.b_get);

        t_port = (EditText) findViewById(R.id.t_port);
        t_word = (EditText) findViewById(R.id.t_word);

        t_result = (TextView) findViewById(R.id.t_result);

        b_Start.setOnClickListener(this);
        b_Get.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == b_Start) {
                Log.d("STATE", "Starting server on port: " + t_port.getText().toString());
                (new ServerThread()).start();

        } else if (v == b_Get) {
            String clientAddress = "localhost";
            String clientPort = t_port.toString();
            String word = t_word.getText().toString();

            t_result.setText("");

            ClientThread clientThread = new ClientThread(clientAddress, parseInt(clientPort), word);
            clientThread.start();
        }

    }
}
