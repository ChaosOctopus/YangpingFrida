package com.tools.yangpingfrida;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(v -> clickStack());
    }

    private void clickStack() {
        Toast.makeText(this,"clickStack",Toast.LENGTH_LONG).show();
    }
}