package com.example.myproject.ui.login;

import android.content.Intent;
import android.os.Bundle;

import com.example.myproject.Main2Activity;
import com.example.myproject.MainActivity;
import com.example.myproject.MapsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Main3Activity extends AppCompatActivity {


    private Button btn;
    private Button btn2;
    private Button btn3;

    private EditText et;

    private boolean deleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);


        btn = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        et = (EditText) findViewById(R.id.editText);




        //database = FirebaseDatabase.getInstance();
        //ref = database.getReference().child("Favorite Places");



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = et.getText().toString();

                deleted = false;

                try{
                    if( name.isEmpty()){

                        Toast.makeText(Main3Activity.this, "Missing name", Toast.LENGTH_SHORT).show();
                    }
                    else{



                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query pQuery = ref.child("Favorite Places").orderByChild("name").equalTo(name);

                        pQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot pSnapshot: dataSnapshot.getChildren()) {
                                    pSnapshot.getRef().removeValue();
                                    deleted = true;
                                }
                                if (deleted == false){
                                    Toast.makeText(Main3Activity.this, "Not Deleted", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(Main3Activity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("TAG", "onCancelled", databaseError.toException());
                            }
                        });





                        //ref.removeValue();


                    }

                }
                catch ( Exception e){

                    Toast.makeText(Main3Activity.this, "Incorrect Inputs", Toast.LENGTH_SHORT).show();

                }



            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivity();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivityOne();
            }
        });

    }

    private void launchActivity() {

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void launchActivityOne() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
