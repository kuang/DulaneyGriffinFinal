package zhiyikuang.dulaneygriffinfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class bookmarkActivity extends AppCompatActivity {
    private ListView bookmarkList;
    private ActionBar ab;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private SharedPreferences.Editor editor;
    private ArrayList<String> bookmarkArrayList;

    private ArrayList<String> URLs;
    private ArrayList<String> Titles;


    private ImageButton drawerToggle;
    private TextView pageTitle;
    private ImageButton deleteToggle;
    private boolean deleteStatus;
    private ImageButton shareButton;



    private final String[] drawerTitles = {"Home","News", "Sports", "Opinion","Favorites"};
    private final String[] griffinLinks = {"http://www.dulaneygriffin.org/","http://www.dulaneygriffin.org/category/news","http://www.dulaneygriffin.org/category/sports","http://www.dulaneygriffin.org/category/opinion","http://www.dulaneygriffin.org/category/features"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        deleteStatus = false;
        bookmarkArrayList           = new ArrayList<>();
        URLs                        = new ArrayList<>();
        Titles                      = new ArrayList<>();
        try {
            readFile();
            getTitles();
            getURLs();
        } catch (IOException e) {}

        ab                          = getSupportActionBar();
        bookmarkList                = (ListView)findViewById(R.id.bookmark_list);
        drawerList                  = (ListView)findViewById(R.id.drawer);
        drawerLayout                = (DrawerLayout)findViewById(R.id.drawer_layout);


        LayoutInflater li = LayoutInflater.from(this);//set custom view to actionbar
        View view = li.inflate(R.layout.actionbar,null);
        ab.setCustomView(view);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        //inflate actionbar items
        drawerToggle                =(ImageButton)findViewById(R.id.drawer_toggle);
        pageTitle                   =(TextView)findViewById(R.id.page_title);
        deleteToggle              = (ImageButton)findViewById(R.id.bookmark_toggle);
        shareButton                 =(ImageButton)findViewById(R.id.shareButton);

        shareButton.setImageResource(0);

        deleteToggle.setImageResource(R.drawable.delete);
        pageTitle.setText("Favorites");

        deleteFinished();
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position<4)
                    //webView.loadUrl(griffinLinks[position]);
                drawerLayout.closeDrawers();
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

        addDrawerItems();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                              @Override
                                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                  if (position < 4) {
                                                      try {
                                                          writeFile();
                                                      } catch (IOException e) {
                                                      }
                                                      Intent i = new Intent(bookmarkActivity.this, MainActivity.class);
                                                      i.putExtra("url_to_load", griffinLinks[position]);
                                                      startActivity(i);
                                                  } else
                                                      drawerLayout.closeDrawers();


                                              }
                                          });






        final ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Titles);
        bookmarkList.setAdapter(mAdapter);


        deleteToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteStatus == false) {
                    deleteStatus=true;
                    ab.setBackgroundDrawable(new ColorDrawable(Color.RED));
                    deleteToggle.setImageResource(R.drawable.done);
                    pageTitle.setText("Press item to delete");
                    bookmarkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            AlertDialog checkSure = new AlertDialog.Builder(bookmarkActivity.this).create();
                            checkSure.setTitle("Are you sure?");
                            checkSure.setMessage("Press Yes to delete");
                            final int positionToRemove = position;
                            checkSure.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            checkSure.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAdapter.remove(positionToRemove);
                                            mAdapter.notifyDataSetChanged();
                                            URLs.remove(positionToRemove);
                                            Titles.remove(positionToRemove);
                                            bookmarkArrayList.remove(positionToRemove);
                                            bookmarkArrayList.remove(positionToRemove);
                                            try {
                                                writeFile();
                                            } catch (IOException e) {

                                            }

                                        }
                                    });
                            checkSure.show();

                        }
                    });
                }
                else{
                    deleteStatus=false;
                    deleteFinished();
                    deleteToggle.setImageResource(R.drawable.delete);
                }

            }
        });
        pageTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFinished();
            }
        });

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
    public void getTitles(){
        for(int i = 0;i<bookmarkArrayList.size();i+=2){
            Titles.add(bookmarkArrayList.get(i));
        }
    }
    public void getURLs(){
        for(int i = 1;i<bookmarkArrayList.size();i+=2){
            URLs.add(bookmarkArrayList.get(i));
        }
    }
    public void addDrawerItems(){
        ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerTitles);
        drawerList.setAdapter(mAdapter);
    }
    public void deleteFinished(){
        pageTitle.setText("Favorites");
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        bookmarkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(bookmarkActivity.this, MainActivity.class);
                i.putExtra("url_to_load", URLs.get(position));
                startActivity(i);

            }
        });
    }



}
