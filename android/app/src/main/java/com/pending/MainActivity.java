/*
package com.pending;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
*/

package com.pending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pending.model.PendingItem;
import com.pending.repository.NetworkCallback;
import com.pending.repository.PendingRepository;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PendingTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testCreateAndFetchTask();
    }

    private void testCreateAndFetchTask() {
        PendingRepository repository = new PendingRepository();

        PendingItem testTask = new PendingItem(
                1L,
                "Test Spring Boot to Supabase",
                "Recruiter",
                "2026-10-20",
                "2026-10-27",
                0,
                "Testing Retrofit network skeleton",
                1
        );
        Log.d(TAG, "Sending task to server...");

        repository.createItem(testTask, new NetworkCallback<PendingItem>() {
            @Override
            public void onSuccess(PendingItem createdItem) {
                Log.d(TAG, "Success! Created task with ID: " + createdItem.getId());
                Toast.makeText(MainActivity.this,
                        "Supabase Success! Task ID: " + createdItem.getId(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Task Creation Error: " + errorMessage);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}

