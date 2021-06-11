package mx.edu.itver.TecnoChat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private ListView chat;
    private HiloMensajes tareaHiloMensajes;

    private Handler mainHandler = new Handler();

    final Context context = this;

    SimpleDateFormat formatterMDY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        // Se extraen los parametros de la Ventana Principal
        String direccionIP = intent.getStringExtra(MainActivity.EXTRA_DIP);
        String usuario = intent.getStringExtra(MainActivity.EXTRA_USR);
        String password = intent.getStringExtra(MainActivity.EXTRA_CON);

        formatterMDY = new SimpleDateFormat("dd MMMM HH:mm");

        listItems = new ArrayList<String>();

        chat = findViewById(R.id.chat);

        // Se requiere un adaptador, para controlar el despliegue de los datos del arreglo en la Lista
        // el adaptador es como la clase Model que almacena los datos de un componente, en Java Standard
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, listItems);

        // Se asocia el adaptador al ListView (chat)
        chat.setAdapter(adapter);

        String response = "";

        // Estas dos lineas se tienen que colocar para evitar un error de aviso de seguridad
        // Esta aplicacion se tiene que convertir a una implementacion con AsyncTask
        // Se deja así para que coincida con la implementación en la practica con Java Estandar
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Se crea la tarea que llevara la recepción de los mensajes
        tareaHiloMensajes = new HiloMensajes(direccionIP, 4444, usuario, password);     //direccionIP: 192.168.100.21
        tareaHiloMensajes.start(); // se inicia la tarea

        findViewById(R.id.btnEnviar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText edtMensaje = findViewById(R.id.edtMensaje);
                String mensaje = edtMensaje.getText().toString();

                addToList(mensaje); // agregamos el mensaje a la lista

                edtMensaje.setText(""); // borramos el texto de la caja de texto

                edtMensaje.onEditorAction(EditorInfo.IME_ACTION_DONE); // escondemos el teclado

                tareaHiloMensajes.enviar(mensaje); // enviamos el mensaje al servidor
            }
        });

    }

    void addToList(String respuesta) {
        String fechaHora = formatterMDY.format(Calendar.getInstance().getTime());
        listItems.add(fechaHora + " < " + respuesta);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        tareaHiloMensajes.cerrar();

        super.onBackPressed();
    }

    public class HiloMensajes extends Thread {

        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        String buffer;

        String direccionIP = "";
        int puerto = 0;
        String usuario = "";
        String password = "";

        ChatActivity parent = null;

        boolean salir = false;

        HiloMensajes(String _direccionIP, int _puerto, String _usuario, String _password) {
            direccionIP = _direccionIP;
            puerto = _puerto;
            usuario = _usuario;
            password = _password;
        }

        public void run() {

            try {
                clientSocket = new Socket(direccionIP, puerto);

                out = new PrintWriter(clientSocket.getOutputStream(), true);

                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                buffer = in.readLine(); // Leer bienvenida

                this.addRespuesta(buffer);

                out.println(usuario);
                out.println(password);

                while (!this.salir) {
                    buffer = in.readLine();

                    if (buffer != null) {
                        this.addRespuesta(buffer);
                    }
                }
            } catch (final IOException ioe) {

                Log.v("IOExcepcion:", ioe.getMessage());

                alerta("IOExcepcion", ioe.getMessage());

            } catch (Exception e) {
                Log.v("Excepcion!:", e.getMessage());
                alerta("Excepcion", e.getMessage());
            }
        }

        void addRespuesta(final String mensaje) {
            // Si no se manda a invocar el metodo addList
            // desde este Handler, marca un error ya que
            // el componente de la Lista no pertenece a este thread
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    addToList(mensaje);
                }
            });
        }

        void enviar(String mensaje) {
            if (out != null) {
                out.println(mensaje);
            } else {
                alerta("Error", "Sin conexión al servidor " + direccionIP + ". Revise su estado de red y la dirección IP especificada.");
            }
        }

        void cerrar() {
            try {
                enviar(usuario + " se ha desconectado");
                this.salir = true;
                clientSocket.close();
                in.close();
                out.close();
            } catch (IOException ex) {
                Log.v("error al cerrar", ex.getMessage());
            }
        }

        void alerta(final String titulo, final String msg) {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (ChatActivity.this != null) {

                        new AlertDialog.Builder(ChatActivity.this.context)
                                .setTitle(titulo)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (ChatActivity.this != null) {
                                            ChatActivity.this.finish();
                                        }
                                    }
                                }).show();
                    }
                }
            });
        }
    }
}