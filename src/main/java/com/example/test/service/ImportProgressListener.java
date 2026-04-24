package com.example.test.service;

@FunctionalInterface
public interface ImportProgressListener {
    void onProgressUpdate(String fileName, int current, int total);
}