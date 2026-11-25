package com.example.vocabmaster.controller;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-memory SharedPreferences fake for pure JVM unit testing.
 */
class InMemorySharedPreferences implements SharedPreferences {

    private final Map<String, Object> values = new HashMap<>();

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<>(values);
    }

    @Override
    public String getString(String key, String defValue) {
        Object val = values.get(key);
        return val instanceof String ? (String) val : defValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Object val = values.get(key);
        return val instanceof Set ? new HashSet<>((Set<String>) val) : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Object val = values.get(key);
        return val instanceof Integer ? (Integer) val : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Object val = values.get(key);
        return val instanceof Long ? (Long) val : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Object val = values.get(key);
        return val instanceof Float ? (Float) val : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Object val = values.get(key);
        return val instanceof Boolean ? (Boolean) val : defValue;
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new InMemoryEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    private class InMemoryEditor implements Editor {
        private final Map<String, Object> pending = new HashMap<>();
        private final Set<String> removals = new HashSet<>();
        private boolean clearRequested = false;

        @Override
        public Editor putString(String key, String value) {
            pending.put(key, value);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            pending.put(key, values != null ? new HashSet<>(values) : null);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            pending.put(key, value);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            pending.put(key, value);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            pending.put(key, value);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            pending.put(key, value);
            removals.remove(key);
            return this;
        }

        @Override
        public Editor remove(String key) {
            removals.add(key);
            pending.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            clearRequested = true;
            pending.clear();
            removals.clear();
            return this;
        }

        @Override
        public boolean commit() {
            apply();
            return true;
        }

        @Override
        public void apply() {
            if (clearRequested) {
                values.clear();
            }
            for (String key : removals) {
                values.remove(key);
            }
            for (Map.Entry<String, Object> entry : pending.entrySet()) {
                if (entry.getValue() == null) {
                    values.remove(entry.getKey());
                } else {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
