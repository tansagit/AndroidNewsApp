package com.app.androidnewsapp.callbacks;

import com.app.androidnewsapp.models.Category;
import com.app.androidnewsapp.models.News;

import java.util.ArrayList;
import java.util.List;

public class CallbackCategoryDetails {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public Category category = null;
    public List<News> posts = new ArrayList<>();
}
