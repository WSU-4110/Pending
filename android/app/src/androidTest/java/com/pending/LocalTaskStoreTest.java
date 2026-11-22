package com.pending;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import com.google.errorprone.annotations.DoNotMock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class LocalTaskStoreTest {

//    @Rule
//    private ActivitySceario testActivity = new ActivityTestRule<>(TaskDetailsActivity.class);

    @Before
    public void launchActivity() {
        ActivityScenario.launch(TaskDetailsActivity.class);
    }

    //Test 1+2: LocalTaskStore: Save and Get
    @Test
    public void SaveGetTest(){
        System.out.println("Starting save and get test");
        Context cont = getApplicationContext();
        LocalTaskStore.Task preTask = new LocalTaskStore.Task(1, "Title", "11/21/26","notes", false);

        LocalTaskStore.save(cont, preTask);
        LocalTaskStore.Task postTask = LocalTaskStore.get(cont, 1);
        System.out.println("LocalTaskStore: Save and Get");

        assertEquals(postTask.id, preTask.id);
        assertEquals(postTask.title, preTask.title);
        assertEquals(postTask.date, preTask.date);
        assertEquals(postTask.notes, preTask.notes);
        assertEquals(postTask.completed, preTask.completed);

        System.out.println("Completed save and get test");

    }

    //Test 3 Get all
    @Test
    public void GetAllTest(){
        class SortbyId implements Comparator<LocalTaskStore.Task>
        {
            public int compare(LocalTaskStore.Task a, LocalTaskStore.Task b)
            {
                return (int)(a.id - b.id);
            }
        }

        Context cont = getApplicationContext();
        List<LocalTaskStore.Task> postTasks,
                preTasks = new ArrayList<>(List.of(
                new LocalTaskStore.Task(1, "Shoppin","11/12/26","Mall",true),
                new LocalTaskStore.Task(2, "Walkin","11/14/26","Mall2",true),
                new LocalTaskStore.Task(3, "Talkin","11/22/26","Mall4",false)
        ));

        for(int i = 0; i < 3; i++){
            LocalTaskStore.save(cont, preTasks.get(i));
        }

        postTasks = LocalTaskStore.all(cont);
        assertEquals(postTasks.size(), preTasks.size());
        Collections.sort(postTasks, new SortbyId());
        Collections.sort(preTasks, new SortbyId());

        for(int i = 0; i < 3; i++){
            LocalTaskStore.Task postTask = postTasks.get(i),
                    preTask = preTasks.get(i);
            System.out.printf("LocalTaskStore: Get All: %1d",i + 1);
            assertEquals(postTask.id, preTask.id);
            assertEquals(postTask.title, preTask.title);
            assertEquals(postTask.date, preTask.date);
            assertEquals(postTask.notes, preTask.notes);
            assertEquals(postTask.completed, preTask.completed);
        }
    }


}
