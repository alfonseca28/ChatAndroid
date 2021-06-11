package mx.edu.itver.TecnoChat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_DIP = "mx.edu.itver.ChatTecno.DIP";
    public final static String EXTRA_USR = "mx.edu.itver.ChatTecno.USR";
    public final static String EXTRA_PAS = "mx.edu.itver.ChatTecno.PAS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnConectar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);

                EditText edtDireccionIP = findViewById(R.id.edtDireccionIP);
                String direccionIP = edtDireccionIP.getText().toString();

                edtDireccionIP.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        System.out.println(keyEvent.getCharacters());
                        return true;
                    }
                });

                EditText edtUsuario = findViewById(R.id.edtUsuario);
                String usuario = edtUsuario.getText().toString();

                EditText edtPassword = findViewById(R.id.edtPassword);
                String password = edtPassword.getText().toString();

                intent.putExtra(EXTRA_DIP, direccionIP);
                intent.putExtra(EXTRA_USR, usuario);
                intent.putExtra(EXTRA_PAS, password);

                startActivity(intent);
            }
        });
    }
}