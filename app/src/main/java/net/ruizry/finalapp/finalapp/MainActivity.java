package net.ruizry.finalapp.finalapp;

import android.content.Intent;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView horizontalItem = (TextView) findViewById(R.id.enterCloud);
        horizontalItem.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,MealPlanner.class));
        }
        });

    }
}
