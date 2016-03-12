package zhiyikuang.dulaneygriffinfinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ObservableScrollViewCallbacks{
    private ObservableWebView webView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ActionBar ab;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ArrayList<String> bookmarkArrayList;
    private ImageButton drawerToggle;
    private TextView pageTitle;
    private ImageButton bookmarkToggle;
    private ImageButton shareButton;




    private final String[] drawerTitles = {"Home","News", "Sports", "Opinion","Favorites"};
    private final String[] griffinLinks = {"http://www.dulaneygriffin.org/","http://www.dulaneygriffin.org/category/news","http://www.dulaneygriffin.org/category/sports","http://www.dulaneygriffin.org/category/opinion","http://www.dulaneygriffin.org/category/features"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File bookmarkFile = new File(getFilesDir(),"bookmark.txt");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bookmarkArrayList = new ArrayList<>();

        try {
            readFile();
        } catch (IOException e) {

        }


        ab                          = getSupportActionBar();

        webView                     = (ObservableWebView)findViewById(R.id.webView);
        mSwipeRefreshLayout         = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        drawerList                  = (ListView)findViewById(R.id.drawer);
        drawerLayout                = (DrawerLayout)findViewById(R.id.drawer_layout);

        Intent i = getIntent();
        String firstURL = i.getStringExtra("url_to_load");
        if(firstURL!=null){
            webView.loadUrl(firstURL);
        }
        else{
            webView.setVisibility(View.INVISIBLE);
            ab.hide();
            webView.loadUrl("http://www.dulaneygriffin.org");}


        LayoutInflater li = LayoutInflater.from(this);//set custom view to actionbar
        View view = li.inflate(R.layout.actionbar,null);
        ab.setCustomView(view);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //inflate actionbar items
        drawerToggle                =(ImageButton)findViewById(R.id.drawer_toggle);
        bookmarkToggle              =(ImageButton)findViewById(R.id.bookmark_toggle);
        pageTitle                   =(TextView)findViewById(R.id.page_title);
        shareButton                 =(ImageButton)findViewById(R.id.shareButton);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {  //checks when user pulls at top
            @Override
            public void onRefresh() {
                webView.reload();//refreshes
            }});

        webView.setScrollViewCallbacks(this);//hiding taskbar when scrolling down



        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setScrollViewCallbacks(this);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {

                if (progress == 100) {
                    mSwipeRefreshLayout.setRefreshing(false);//stops infinite loading animation
                    webView.setVisibility(View.VISIBLE);
                    ab.show();
                    drawerLayout.closeDrawer(Gravity.LEFT);
                   }}
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title)) {
                    pageTitle.setText(webView.getTitle().substring(14,webView.getTitle().length()));
                    if(!bookmarkArrayList.contains(webView.getUrl()))
                            bookmarkToggle.setImageResource(R.drawable.bookmark_blank);
                    else
                        bookmarkToggle.setImageResource(R.drawable.bookmark_filled);
                }
            }
        });
        bookmarkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String url = webView.getUrl();
            String title = pageTitle.getText().toString();
                    if(!bookmarkArrayList.contains(url)){
                        bookmarkToggle.setImageResource(R.drawable.bookmark_filled);
                        bookmarkArrayList.add(title);
                        bookmarkArrayList.add(url);

                        }
                    else{
                        bookmarkToggle.setImageResource(R.drawable.bookmark_blank);
                        bookmarkArrayList.remove(title);
                        bookmarkArrayList.remove(url);


                    }
                try {
                    writeFile();
                } catch (IOException e) {

                }

            }
        });

        addDrawerItems();




        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position<4)
                    webView.loadUrl(griffinLinks[position]);
                else{
                    Intent i = new Intent(MainActivity.this,bookmarkActivity.class);
                    try {
                        writeFile();
                    } catch (IOException e) {   }
                    startActivity(i);
                    drawerLayout.closeDrawers();


                }
                }


        });
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!drawerLayout.isDrawerOpen(drawerList))
                drawerLayout.openDrawer(drawerList);
                else
                    drawerLayout.closeDrawers();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                startActivity(i);
            }
        });





    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }
    public void addDrawerItems(){
        ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerTitles);
        drawerList.setAdapter(mAdapter);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawers();
            return true;
        }
        else if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            //if Back key pressed and webview can navigate to previous page
            webView.goBack();
            // go back to previous page
            return true;
        }
        else
        {
            finish();
            // finish the activity
        }
        return super.onKeyDown(keyCode, event);
    }
    public void readFile() throws IOException {
        FileInputStream fis = this.openFileInput("bookmark.txt");
        Scanner s = new Scanner(fis);
        while (s.hasNextLine()) {
            bookmarkArrayList.add(s.nextLine());
        }
    }
    public void writeFile() throws IOException {
        FileOutputStream fos;
        PrintStream ps;
        try{
            fos = openFileOutput("bookmark.txt", Context.MODE_PRIVATE);
            ps = new PrintStream(fos);
            for(int i =0;i<bookmarkArrayList.size();i++){
                ps.write(bookmarkArrayList.get(i).getBytes());
                ps.println();
            }
            ps.flush();
            ps.close();
            fos.flush();
            fos.close();

        }
        catch(Exception e){}


    }


    }



