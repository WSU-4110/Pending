package com.pending;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class LocalTaskStore {

    private static final String PREFS = "pending_sprint1_demo";
    private static final String KEY = "tasks";

    private LocalTaskStore() {}

    public static class Task {
        public long id;
        public String title;
        public String date;
        public String notes;
        public boolean completed;

        public Task(long id, String title, String date,
                    String notes, boolean completed) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.notes = notes;
            this.completed = completed;
        }
    }

    public static List<Task> all(Context context) {
        List<Task> result = new ArrayList<>();

        String json = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE
        ).getString(KEY, "[]");

        try {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);

                result.add(new Task(
                        o.getLong("id"),
                        o.optString("title"),
                        o.optString("date"),
                        o.optString("notes"),
                        o.optBoolean("completed")
                ));
            }
        } catch (Exception ignored) {}

        return result;
    }

    private static void write(Context context, List<Task> tasks) {
        JSONArray array = new JSONArray();

        try {
            for (Task t : tasks) {
                JSONObject o = new JSONObject();
                o.put("id", t.id);
                o.put("title", t.title);
                o.put("date", t.date);
                o.put("notes", t.notes);
                o.put("completed", t.completed);
                array.put(o);
            }
        } catch (Exception ignored) {}

        context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE
        ).edit().putString(KEY, array.toString()).apply();
    }

    public static void save(Context context, Task task) {
        List<Task> tasks = all(context);
        boolean found = false;

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id == task.id) {
                tasks.set(i, task);
                found = true;
                break;
            }
        }

        if (!found) {
            tasks.add(0, task);
        }

        write(context, tasks);
    }

    public static Task get(Context context, long id) {
        for (Task task : all(context)) {
            if (task.id == id) {
                return task;
            }
        }
        return null;
    }

    public static void delete(Context context, long id) {
        List<Task> tasks = all(context);
        tasks.removeIf(task -> task.id == id);
        write(context, tasks);
    }
}
