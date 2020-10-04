package com.yuvraj.passwordgeneratorandmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               Key_Generator_Fragment.Key_Generator_Fragment_Listener,
                                                               Vault_Fragment.Vault_Fragment_Listener,
                                                               create_vault_dialog.create_vault_dialog_listener,
                                                               delete_vault_dialog.delete_vault_dialog_listener,
                                                               enter_new_pass_dialog.enter_new_pass_dialog_listener,
                                                               Sync_Fragment.sync_fragment_listener,
                                                               about_dialog.about_dialog_listener{

    //android ready_made resource
    DrawerLayout drawer_layout;
    ActionBarDrawerToggle action_bar_toggle;
    Toolbar toolbar;
    NavigationView navigation_view;
    FragmentManager fragment_manager;
    FragmentTransaction fragment_transaction;
    ClipboardManager clipboard;
    //self made resources
    private database_handler vault_db;
    public ArrayList<vault_data> vault_data_list;
    private enter_new_pass_dialog enterNewPassDialog;
    private create_vault_dialog createVaultDialog;
    private delete_vault_dialog deleteVaultDialog;
    private about_dialog aboutDialog;
    private ArrayList<String[]> table_name_and_vault_name_list;
    private boolean vault_open=false;
    public boolean delete_dialog_start=false,open_vault_dialog_start=false;
    private Vault_Fragment vaultFragment;

    //sync_fragment variables
    private Sync_Fragment syncFragment;
    public boolean is_signed_in=false;
    private GoogleSignInClient mGoogleSignInClient;
    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 100;
    private int current_fragment_code=-1;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();//for the sync now thread
    private java.io.File zip_file;//used by sync_download() function
    private SharedPreferences settings_reader;
    private SharedPreferences.Editor settings_editor;
    private TextView drawer_header_text_view;
    private ImageView drawer_header_imageView;
    public boolean sync_lock=false;
    private CheckBox redScheme,greenScheme,greyScheme,blueScheme,violetScheme,pinkScheme;
    ArrayList<CheckBox> checkBox_List=new ArrayList<>();
    private boolean dark_mode_on=true;
    private int current_color_scheme=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        color_scheme_changer(settings_reader.getInt("color_scheme_code", 3),true);
        save_color_scheme_settings(settings_reader.getInt("color_scheme_code",3));
        current_color_scheme=settings_reader.getInt("color_scheme_code", 3);

        setContentView(R.layout.activity_main);

        //main activity elements
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);
        navigation_view = findViewById(R.id.navigation_view);

        navigation_view.setNavigationItemSelectedListener(this);
        action_bar_toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.open, R.string.close);
        drawer_layout.addDrawerListener(action_bar_toggle);
        navigation_view.setItemIconTintList(null);//for default colour icons
        action_bar_toggle.setDrawerIndicatorEnabled(true);
        action_bar_toggle.syncState();
        deleteVaultDialog = new delete_vault_dialog();
        aboutDialog = new about_dialog();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.DarkGrey));
        window.setNavigationBarColor(getResources().getColor(R.color.Black, null));
        //load default fragment
        if(savedInstanceState!=null)
        {
            Map<String,Integer> map=get_color_id();
            String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
            map.clear();
            if(savedInstanceState.getInt("current_fragment_code")==1)
            {
                getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Password Generator" + "</font>"));
                fragment_manager = getSupportFragmentManager();
                fragment_manager.beginTransaction().replace(R.id.container_fragment, new Key_Generator_Fragment(), "key_generator_fragment").commit();
                current_fragment_code=1;
            }
            else if (savedInstanceState.getInt("current_fragment_code") == 2)
            {
                fragment_manager = getSupportFragmentManager();
                fragment_manager.beginTransaction().replace(R.id.container_fragment, new Vault_Fragment(), "vaultFragment").commit();
                getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Local Vault" + "</font>"));
                current_fragment_code=2;
            }
            else if (savedInstanceState.getInt("current_fragment_code") == 3)
            {
                fragment_manager = getSupportFragmentManager();
                fragment_manager.beginTransaction().replace(R.id.container_fragment, new Sync_Fragment(), "syncFragment").commit();
                getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Cloud Sync & Local Backup" + "</font>"));
                current_fragment_code=3;
            }
        }
        else
        {
            Map<String,Integer> map=get_color_id();
            String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
            map.clear();
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Password Generator" + "</font>"));
            fragment_manager = getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment, new Key_Generator_Fragment(), "key_generator_fragment").commit();
            current_fragment_code = 1;
        }
        //main activity functions
        clipboard=(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        vault_db=new database_handler(this,true);
        vault_data_list=new ArrayList<>();

        //auto sync settings
        //Drawer settings
        //drawer header element handler
        View drawer_header_view=navigation_view.inflateHeaderView(R.layout.drawer_header);
        drawer_header_text_view=(TextView) drawer_header_view.findViewById(R.id.drawer_header_textView);
        drawer_header_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_sign_in_status();
                if(!is_signed_in)
                {   sign_in_with_google();}
                else
                {
                    PopupMenu  popupMenu = new PopupMenu(MainActivity.this,drawer_header_text_view);
                    popupMenu.getMenuInflater().inflate(R.menu.drawer_header_menu,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            sign_out();
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            }
        });
        drawer_header_imageView=(ImageView)drawer_header_view.findViewById(R.id.drawer_header_imageView);
        drawer_header_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_sign_in_status();
                if(!is_signed_in)
                {   sign_in_with_google();}
                else
                {
                    PopupMenu  popupMenu = new PopupMenu(MainActivity.this,drawer_header_imageView);
                    popupMenu.getMenuInflater().inflate(R.menu.drawer_header_menu,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            sign_out();
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            }
        });
        ImageView headerBackground=drawer_header_view.findViewById(R.id.headerBackground);
        Glide.with(this).load(R.drawable.drawer_header_background).into(headerBackground);
        //Theme settings
        LinearLayout drawer_item_linear_layout=(LinearLayout)navigation_view.getMenu().findItem(R.id.theme_menu_item).getActionView();
        /*Switch dark_mode_switch = drawer_item_linear_layout.findViewById(R.id.dark_mode_switch);
        dark_mode_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_dark_mode();
            }
        });
        dark_mode_switch.setVisibility(View.GONE);*/
        redScheme =  drawer_item_linear_layout.findViewById(R.id.red_color_scheme);
        redScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(0,false);
            }
        });
        greenScheme =  drawer_item_linear_layout.findViewById(R.id.green_color_scheme);
        greenScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(1,false);
            }
        });
        greyScheme =  drawer_item_linear_layout.findViewById(R.id.grey_color_scheme);
        greyScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(2,false);
            }
        });
        blueScheme =  drawer_item_linear_layout.findViewById(R.id.blue_color_scheme);
        blueScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(3,false);
            }
        });
        violetScheme =  drawer_item_linear_layout.findViewById(R.id.violet_color_scheme);
        violetScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(4,false);
            }
        });
        pinkScheme =  drawer_item_linear_layout.findViewById(R.id.pink_color_scheme);
        pinkScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color_scheme_changer(5,false);
            }
        });
        checkBox_List.add(redScheme);
        checkBox_List.add(greenScheme);
        checkBox_List.add(greyScheme);
        checkBox_List.add(blueScheme);
        checkBox_List.add(violetScheme);
        checkBox_List.add(pinkScheme);
        //sign in functions
        GoogleSignInAccount account=get_sign_in_status();
        if(is_signed_in)
        {   load_account_image_and_id(account);}
        else
        {   reset_account_image_and_id();}

        change_ui_element_based_on_theme(settings_reader.getInt("color_scheme_code", 1));
        //handling auto sync
        if(settings_reader.getBoolean("auto_sync_state", false) && is_signed_in)
        {   sync_now(false);}
    }
    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("current_fragment_code",current_fragment_code);
        super.onSaveInstanceState(state);
    }
    //-----------------------------------------------------------------------------------------About Dialog---------------------------------------------------------------------------------------------------------
    @Override
    public void about_dialog_ok()
    {   aboutDialog.dismiss();}
    //----------------------------------------------------------------------------------------Drawer Layout---------------------------------------------------------------------------------------------------------
    @Override
    public Map<String,Integer> get_color_id()
    {
        Map<String,Integer> map=new HashMap<>();

        TypedValue typedValue1 = new TypedValue();
        getTheme().resolveAttribute(R.attr.DeepColor, typedValue1, true);
        map.put("DeepColor",ContextCompat.getColor(this, typedValue1.resourceId));

        TypedValue typedValue2 = new TypedValue();
        getTheme().resolveAttribute(R.attr.MediumColor, typedValue2, true);
        map.put("MediumColor",ContextCompat.getColor(this, typedValue2.resourceId));

        return map;
    }
    private void toggle_dark_mode()
    {

    }
    private void change_ui_element_based_on_theme(int color_scheme_code)
    {
        for(int a=0;a<checkBox_List.size();a++)
        {
            if(a==color_scheme_code)
            {   checkBox_List.get(a).setChecked(true);}
            else
            {   checkBox_List.get(a).setChecked(false);}
        }
    }
    private void save_color_scheme_settings(int color_scheme)
    {
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        settings_editor = getSharedPreferences("settings",Context.MODE_PRIVATE).edit();
        settings_editor.putInt("color_scheme_code", color_scheme);
        settings_editor.apply();///commit()
    }
    private void color_scheme_changer(int color_scheme_code,boolean first_start)
    {
        if(!first_start && current_color_scheme!=color_scheme_code)
        {
            save_color_scheme_settings(color_scheme_code);
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }
        else if(!first_start && current_color_scheme==color_scheme_code)
        {   checkBox_List.get(color_scheme_code).setChecked(true);}
        if(color_scheme_code==0 && first_start)
        {   setTheme(R.style.DarkRedTheme_NoActionBar);}
        else if(color_scheme_code==1 && first_start)
        {    setTheme(R.style.DarkGreenTheme_NoActionBar);}
        else if(color_scheme_code==2 && first_start)
        {   setTheme(R.style.DarkGreyTheme_NoActionBar);}
        else if(color_scheme_code==3 && first_start)
        {   setTheme(R.style.DarkBlueTheme_NoActionBar);}
        else if(color_scheme_code==4 && first_start)
        {   setTheme(R.style.DarkVioletTheme_NoActionBar);}
        else if(color_scheme_code==5 && first_start)
        {   setTheme(R.style.DarkPinkTheme_NoActionBar);}
    }
    private void reset_account_image_and_id()
    {
        Glide.with(this).load(R.drawable.person_icon).circleCrop().into(drawer_header_imageView);
        drawer_header_text_view.setText("Sign In");
    }
    private void load_account_image_and_id(GoogleSignInAccount account)
    {
        if(is_signed_in) {
            try {
                if(account.getPhotoUrl()!=null)
                {   Glide.with(this).load(account.getPhotoUrl()).circleCrop().into(drawer_header_imageView);}
                else
                {   Glide.with(this).load(R.drawable.person_icon).circleCrop().into(drawer_header_imageView);}
                drawer_header_text_view.setText(account.getDisplayName());
            }
            catch(Exception e)
            { e.printStackTrace();}
        }
    }
    //------------------------------------------------------------------------------------sync fragment functions----------------------------------------------------------------------------------------------------
    @Override
    public void send_auto_sync_click_signal()
    {
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        settings_editor = getSharedPreferences("settings",Context.MODE_PRIVATE).edit();
        if(settings_reader.getBoolean("auto_sync_state", false))
        {   settings_editor.putBoolean("auto_sync_state", false);}
        else
        {   settings_editor.putBoolean("auto_sync_state", true);}
        settings_editor.apply();///commit()
    }
    @Override
    public boolean is_auto_sync_active()
    {
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        return settings_reader.getBoolean("auto_sync_state", false);
    }
    @Override
    public void local_backup_restore(int start_code) {
        Map<String,Integer> map=get_color_id();
        String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
        if (start_code == 1)//perform local backup
        {
            materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Permission required..</font>"));
            materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">To perform local backup permission is required to save the backup file in your local storage. Please press ok and select grant permission to proceed further.</font>"));
        }
        else if(start_code==2)//load backup file
        {
            materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Permission required..</font>"));//both the title are same
            materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">To load backup file permission is required to read you local storage. Please press ok and select grant permission to proceed further.</font>"));
        }
        materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

        materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (start_code == 1) {
                    Intent file_explorer_intent = new Intent(MainActivity.this, File_Explorer_Activity.class);
                    file_explorer_intent.putExtra("file_explorer_intent_start_motive", "backup_file");
                    startActivityForResult(file_explorer_intent, 1);
                }
                else if (start_code == 2)
                {
                    Intent file_explorer_intent = new Intent(MainActivity.this, File_Explorer_Activity.class);
                    file_explorer_intent.putExtra("file_explorer_intent_start_motive", "load_file");
                    startActivityForResult(file_explorer_intent, 2);
                }
            }
        });
        materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        materialAlertDialogBuilder.show();
    }
    private boolean check_internet_connection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    private boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) { return false; }
    }
    private String get_date_and_time()
    {
        int y= Calendar.getInstance().get(Calendar.YEAR);
        int m=Calendar.getInstance().get(Calendar.MONTH)+1;
        int d=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        long t=Calendar.getInstance().getTime().getTime();
        return y+"_"+m+"_"+d+"_"+t;
    }
    private int add_zip_data_to_database(ArrayList<String> zip_entries,ArrayList<ArrayList<String>> zip_data_entry_wise)
    {
        database_handler vault_db=new database_handler(this,true);
        int count1=0,error_code=0;
        String temp="";
        ArrayList<vault_data> vault_data_list=new ArrayList<>();
        ArrayList<database_handler.vault_data_and_error_status> vault_data_and_error_status_list=new ArrayList<>();
        for(int a=0;a<zip_entries.size();a++)
        {
            if(zip_entries.get(a).equals("META_DATA"))
            {   continue;}
            for(int b=1;b<zip_data_entry_wise.get(a).size();b++)//line 0 contains the column names
            {
                count1=0;
                vault_data vaultData=new vault_data();
                for(int c=0;c<zip_data_entry_wise.get(a).get(b).length();c++)
                {
                    if(zip_data_entry_wise.get(a).get(b).charAt(c)==',')
                    {
                        if(count1==0)//id
                        {   vaultData.id=Integer.parseInt(temp);temp="";}
                        else if(count1==1)//ACCOUNT_TYPE_NAME
                        {   vaultData.account_type=temp;temp="";}
                        else if(count1==2)//ACCOUNT_LOGIN_ID
                        {   vaultData.account_id=temp;temp="";}
                        else if(count1==3)//PASSWORD
                        {   vaultData.account_password=temp;temp="";}
                        else if(count1==4)//ENTRY_DATE
                        {   vaultData.date_of_modification=temp;temp="";}
                        else if(count1==5)//IS_META
                        {   vaultData.is_meta_data=Integer.parseInt(temp);temp="";}
                        else if(count1==6)//VAULT_NAME
                        {   vaultData.vault_name=temp;temp="";}
                        count1++;
                    }
                    else
                    {   temp+=zip_data_entry_wise.get(a).get(b).charAt(c);}
                }
                vault_data_list.add(vaultData);
            }
            database_handler.vault_data_and_error_status vault=new database_handler.vault_data_and_error_status(0,vault_data_list);
            vault.table_name=zip_entries.get(a);
            vault_data_and_error_status_list.add(vault);
            vault_data_list=new ArrayList<>();
        }
        vault_db.restore_backup(vault_data_and_error_status_list);
        vault_db.close();
        vault_data_and_error_status_list.clear();
        return error_code;
    }
    private java.io.File get_zipped_backup_file(String zip_file_name)//used to create the to be uploaded zip fie
    {
        //getting the meta data from the database and initializing the required database variables.
        database_handler vault_db=new database_handler(this,true);
        ArrayList<String[]> table_name_array_list=vault_db.get_table_name_vault_names();
        //compression of data
        DocumentFile zip_file;
        java.io.File java_zip_file;
        try
        {
            DocumentFile curDocumentFile=DocumentFile.fromFile( getApplicationContext().getFilesDir());
            zip_file_name=zip_file_name+get_date_and_time()+".zip";
            curDocumentFile.createFile("",zip_file_name);
            zip_file=curDocumentFile.findFile(zip_file_name);
            java_zip_file=new java.io.File(getApplicationContext().getFilesDir()+"/"+zip_file_name);
            OutputStream out=getContentResolver().openOutputStream(zip_file.getUri());
            //creating the mete data file.
            String meta_data_file_content="no_of_tables,date_for_creation,\n"+table_name_array_list.size()+","+get_date_and_time()+",";
            ZipEntry entry1=new ZipEntry("META_DATA");
            entry1.setTime(zip_file.lastModified());
            ZipOutputStream zip_out = new ZipOutputStream(new BufferedOutputStream(out));
            zip_out.putNextEntry(entry1);
            zip_out.write(meta_data_file_content.getBytes());
            for(int a=0;a<table_name_array_list.size();a++)
            {
                ZipEntry entry = new ZipEntry(table_name_array_list.get(a)[0]);
                entry.setTime(zip_file.lastModified()); // to keep modification time after unzipping
                zip_out.putNextEntry(entry);
                zip_out.write(vault_db.get_raw_data_from_table_string(table_name_array_list.get(a)[0]).getBytes());
            }
            zip_out.close();
            out.close();
        }
        catch(Exception e)
        {
            System.out.println("Failed zip!!");
            e.printStackTrace();
            java_zip_file=null;
        }
        //clearing of data
        vault_db.close();
        table_name_array_list.clear();
        return java_zip_file;
    }
    private int load_backup_file(java.io.File backup_file)
    {
        int error_code=0;
        try
        {
            DocumentFile zip_file=DocumentFile.fromFile(backup_file);
            if(zip_file==null)
            {   error_code=3;}
            InputStream in1 = getContentResolver().openInputStream(zip_file.getUri());
            ZipInputStream zip_in1=new ZipInputStream(new BufferedInputStream(in1));
            ZipEntry entry;
            ArrayList<ArrayList<String>> zip_data_entry_wise=new ArrayList<>();
            ArrayList<String> zip_entry_name_list=new ArrayList<>();
            boolean meta_data_found=false;
            while((entry=zip_in1.getNextEntry())!=null)
            {
                zip_entry_name_list.add(entry.getName());
                if(entry.getName().equals("META_DATA"))
                {   meta_data_found=true;}
            }
            zip_in1.close();
            in1.close();
            InputStream in2 = getContentResolver().openInputStream(zip_file.getUri());
            ZipInputStream zip_in2=new ZipInputStream((new BufferedInputStream(in2)));
            if(meta_data_found)
            {
                while ((entry = zip_in2.getNextEntry()) != null)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zip_in2));
                    String line;
                    ArrayList<String> lines=new ArrayList<>();
                    while ((line = reader.readLine()) != null)
                    {  lines.add(line);}
                    zip_data_entry_wise.add(lines);
                }
                zip_in2.close();
                in2.close();
                error_code=add_zip_data_to_database(zip_entry_name_list,zip_data_entry_wise);//error code is not assigned to this function. In future it may be done.
                zip_data_entry_wise.clear();
                zip_entry_name_list.clear();
            }
            else
            {   error_code=1;}
        }
        catch(Exception e)
        {
            if(error_code!=3)
            {   error_code=2;}
            System.out.println("Failed to load backup file.");
            e.printStackTrace();
        }
        return error_code;
    }
    private boolean sync_upload()
    {
        try {
            java.io.File java_zip_file = get_zipped_backup_file("PasswordGeneratorAndManagerBackup");
            if (java_zip_file != null) {
                mDriveServiceHelper.uploadFile(java_zip_file.getName(), java_zip_file).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        task.getResult();
                        Toast.makeText(getApplicationContext(), "Sync complete.", Toast.LENGTH_SHORT).show();
                        syncFragment = (Sync_Fragment) getSupportFragmentManager().findFragmentByTag("syncFragment");
                        if(syncFragment!=null)
                        {
                            syncFragment.sync_now_button.setEnabled(true);
                            syncFragment.sync_now_button.setText(getResources().getText(R.string.sync_button_text));
                        }
                        sync_lock=false;
                    }
                });
            }
            return true;
        }
        catch(Exception e)
        {   e.printStackTrace();return false;}
    }
    private boolean sync_download()
    {
        System.out.println("Sync_download started!!!!");
        try
        {
            mDriveServiceHelper.queryFiles().addOnCompleteListener(new OnCompleteListener<FileList>() {
                @Override
                public void onComplete(@NonNull Task<FileList> task) {
                    List<File> file_list = task.getResult().getFiles();
                    if(file_list.size()==0)
                    {
                        mDriveServiceHelper.delete_backup_file().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sync_upload();
                            }
                        });
                    }
                    for (int a = 0; a < file_list.size(); a++)
                    {   //the file list should contain only one file
                        System.out.println("Name=" + file_list.get(a).getName());
                        if(file_list.get(a).getName().contains("PasswordGeneratorAndManagerBackup") && file_list.get(a).getName().contains(".zip"))
                        {
                            ///download the file
                            System.out.println("Download file");
                            zip_file=new java.io.File(getApplicationContext().getFilesDir()+"/"+file_list.get(a).getName());
                            try {
                                zip_file.createNewFile();
                                mDriveServiceHelper.downloadFile(zip_file,file_list.get(a).getId()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Boolean> task) {
                                        if(task.getResult())
                                        {
                                            //load the backup in the database
                                            int e=load_backup_file(zip_file);
                                            System.out.println("Error code="+e);
                                            zip_file.delete();
                                            mDriveServiceHelper.delete_backup_file().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    sync_upload();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            catch(Exception e){}
                            break;
                        }
                    }
                }
            });
            return true;
        }
        catch(Exception e)
        {   e.printStackTrace();return false;}
    }
    private Task<ArrayList<Boolean>> sync_thread_start(boolean first_time_sync) {
        return Tasks.call(mExecutor, () ->
            {
                //pre task checks
                boolean internet_available = check_internet_connection(), is_online = false;
                if (internet_available) {
                    is_online = isOnline();
                }
                if (mDriveServiceHelper == null && is_online) {
                    initialize_google_drive_service_helper();
                }
                if (is_signed_in && mDriveServiceHelper != null && is_online)
                {
                    if(first_time_sync)
                    {   sync_download();}
                    else
                    {
                        mDriveServiceHelper.delete_backup_file().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sync_upload();
                            }
                        });
                    }
                }
                ArrayList<Boolean> status_list=new ArrayList<>();
                status_list.add(is_online);
                status_list.add(is_signed_in);
                return status_list;
            });
    }
    @Override
    public void sync_now(boolean first_time_sync)
    {
        if(!sync_lock) {
            sync_lock = true;
            syncFragment = (Sync_Fragment) getSupportFragmentManager().findFragmentByTag("syncFragment");
            if (syncFragment != null)
            {
                syncFragment.sync_now_button.setEnabled(false);
                syncFragment.sync_now_button.setText(getResources().getText(R.string.syncing));
            }
            sync_thread_start(first_time_sync).addOnCompleteListener(new OnCompleteListener<ArrayList<Boolean>>() {
                @Override
                public void onComplete(@NonNull Task<ArrayList<Boolean>> task) {
                    ArrayList<Boolean> status_list = task.getResult();
                    if (!status_list.get(0))//is_online
                    {
                        Toast.makeText(getApplicationContext(), "Please check you internet connection.", Toast.LENGTH_SHORT).show();
                        if(syncFragment!=null) {
                            syncFragment.sync_now_button.setEnabled(true);
                            syncFragment.sync_now_button.setText(getResources().getText(R.string.sync_button_text));
                        }
                        sync_lock=false;
                    }
                    else if (!status_list.get(1))//is_signed_in
                    {
                        Toast.makeText(getApplicationContext(), "Please sign in with google first.", Toast.LENGTH_SHORT).show();
                        if(syncFragment!=null) {
                            syncFragment.sync_now_button.setEnabled(true);
                            syncFragment.sync_now_button.setText(getResources().getText(R.string.sync_button_text));
                        }
                        sync_lock=false;
                    }
                }
            });
        }
    }
    public GoogleSignInAccount get_sign_in_status()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account==null)
        {   is_signed_in=false;
            System.out.println("Not Signed in");
        }
        else
        {   is_signed_in=true;
            System.out.println("Signed in");
        }
        return account;
    }
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(getApplicationContext(), signInOptions);
    }
    @Override
    public void sign_in_with_google()
    {
        mGoogleSignInClient=buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int request, int result, Intent data)//for the account selection activity
    {
        System.out.println("request="+request+" result="+result);
        if(data==null)
        {   System.out.println("DATA IS NULL");}
        if(request==100 && result==1)
        {   Toast.makeText(getApplicationContext(), "No account selected.", Toast.LENGTH_SHORT).show();}
        else if(request==100)
        {
            sign_in_handler(data);
        }
        super.onActivityResult(request,result,data);
    }
    private void sign_in_handler(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                System.out.println("Sign in successful.");
                is_signed_in = true;
                syncFragment = (Sync_Fragment) getSupportFragmentManager().findFragmentByTag("syncFragment");
                if(syncFragment!=null)
                {   syncFragment.check_sign_in_status(GoogleSignIn.getLastSignedInAccount(getApplicationContext()).getEmail());}
                initialize_google_drive_service_helper();
                Toast.makeText(getApplicationContext(), "Sign in complete.", Toast.LENGTH_SHORT).show();
                sync_now(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Failed to sign in. Cause: ");
                e.printStackTrace();
            }
        });
    }
    private void initialize_google_drive_service_helper()
    {
        if (is_signed_in)
        {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            com.google.api.services.drive.Drive googleDriveService = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential).setApplicationName("AppName").build();
            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
            load_account_image_and_id(account);
        }
    }
    @Override
    public void sign_out()
    {
        String account_name=GoogleSignIn.getLastSignedInAccount(getApplicationContext()).getEmail();
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
        Map<String,Integer> map=get_color_id();
        String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Sign Out</font>"));
        materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">Are you sure you want to sign out?</font>"));
        materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));
        materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mGoogleSignInClient=buildGoogleSignInClient();
                mGoogleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Sign out successful.");
                        is_signed_in=false;
                        syncFragment = (Sync_Fragment) getSupportFragmentManager().findFragmentByTag("syncFragment");
                        if(syncFragment!=null)
                        {   syncFragment.check_sign_in_status(null);}
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Sign out failed. Cause: "+e.getStackTrace());
                    }
                });
                Toast.makeText(getApplicationContext(), "Signed Out of "+account_name, Toast.LENGTH_SHORT).show();
                reset_account_image_and_id();
            }
        });
        materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        materialAlertDialogBuilder.show();
    }
    //--------------------------------------------------------------------------Key_Generator_fragment interface functions--------------------------------------------------------------------------------------------
    private String generated_password;
    @Override
    public void add_to_vault_button(String pass)
    {
        if(pass.isEmpty())
        {   Toast.makeText(getApplicationContext(),"Please generate a password first.", Toast.LENGTH_SHORT).show();}
        else {
            generated_password=pass;
            if (is_a_vault_open()) {
                enter_new_password_dialog(0);
            }
            else {
                Map<String,Integer> map=get_color_id();
                String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
                String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
                map.clear();
                final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
                materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Vault not opened..</font>"));
                materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">You need to open a vault to save the generated password.</font>"));
                materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

                materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Open Vault</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        open_vault_dialog(0);
                    }
                });
                materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">Cancel</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                materialAlertDialogBuilder.show();
            }
        }
    }
    @Override
    public void on_copied_pass_sent(CharSequence pass)
    {
        ClipData clip=ClipData.newPlainText("NewPassword",pass);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(),"Password Copied", Toast.LENGTH_SHORT).show();
    }

    //------------------------------------------------------------------------------Vault_Fragment interface functions--------------------------------------------------------------------------------------------------
    //add new pass dialog functions
    @Override
    public void paste_id()
    {
        if(!(clipboard.hasPrimaryClip())) {
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
        }
        else
        {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            enterNewPassDialog.set_enter_account_id_EditText(item.getText().toString());
        }
    }
    @Override
    public void paste_account_type()
    {
        if(!(clipboard.hasPrimaryClip())) {
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
        }
        else
        {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            enterNewPassDialog.set_enter_account_type_EditText(item.getText().toString());
        }
    }
    @Override
    public void paste_password()
    {
        if(!(clipboard.hasPrimaryClip())) {
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
        }
        else
        {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            enterNewPassDialog.set_enter_account_password_EditText(item.getText().toString());
        }
    }

    //enter new pass dialog functions
    public  boolean pending_data_entry=false;
    public vault_data pending_data;
    @Override
    public void enter_new_pass_dialog_ok_button(String id,String type,String pass,int called_from_id)
    {
        if(id.isEmpty())
        {   Toast.makeText(getApplicationContext(),"Please enter account id.", Toast.LENGTH_SHORT).show();}
        else if(type.isEmpty())
        {   Toast.makeText(getApplicationContext(),"Please account type.", Toast.LENGTH_SHORT).show();}
        else if(pass.isEmpty())
        {   Toast.makeText(getApplicationContext(),"Please enter password.", Toast.LENGTH_SHORT).show();}
        else
        {
            int error_code=vault_db.add_data_to_table(id,type,pass);
            if(error_code==0) {
                vault_data obj = vault_db.get_last_entered_data();
                if (called_from_id == 1)
                {
                    vaultFragment = (Vault_Fragment) getSupportFragmentManager().findFragmentByTag("vaultFragment");
                    vaultFragment.add_single_data_to_recycle_view(new vault_data(obj.id, obj.account_type, obj.account_id, obj.account_password, obj.date_of_modification, obj.is_meta_data, obj.vault_name));
                }
                else if(called_from_id==0)
                {
                    pending_data_entry=true;
                    pending_data=new vault_data(obj.id, obj.account_type, obj.account_id, obj.account_password, obj.date_of_modification, obj.is_meta_data, obj.vault_name);
                }
                Toast.makeText(getApplicationContext(),"Data entered successfully.", Toast.LENGTH_SHORT).show();
                enterNewPassDialog.dismiss();
            }
            else
            {   Toast.makeText(getApplicationContext(),"Data entry failed.", Toast.LENGTH_SHORT).show();}
        }
    }
    @Override
    public void enter_new_pass_dialog_cancel()
    {
        enterNewPassDialog.dismiss();
    }
    @Override
    public void enter_new_password_dialog(int called_from)
    {
        enterNewPassDialog=new enter_new_pass_dialog();
        enterNewPassDialog.called_from=called_from;
        if(called_from==0)
        {   enterNewPassDialog.generated_pass=generated_password;}
        enterNewPassDialog.show(getSupportFragmentManager(),"EnterNewPassDialog");
    }

    //open vault dialog functions
    public boolean is_a_vault_open()
    {   return vault_open;}
    @Override
    public void open_vault_dialog(int called_from)
    {
        open_vault_dialog_start=true;
        table_name_and_vault_name_list=vault_db.get_table_name_vault_names();
        deleteVaultDialog.called_from_option=called_from;
        deleteVaultDialog.show(getSupportFragmentManager(),"DeleteVaultDialog");
    }
    private void open_vault(int spinner_position,String password,int called_from)
    {
        database_handler.vault_data_and_error_status obj=vault_db.get_data_from_table(spinner_position,
                                                                                      table_name_and_vault_name_list.get(spinner_position)[0],
                                                                                      table_name_and_vault_name_list.get(spinner_position)[1],
                                                                                      password);;
        if(obj.get_data_access_error_code()==1)
        {   System.out.println("Error! Table meta data is not updated.");}
        else if(obj.get_data_access_error_code()==2)
        {   System.out.println("Error! Table meta data indexing problem.");}
        else if(obj.get_data_access_error_code()==3)
        {   Toast.makeText(getApplicationContext(), "Vault failed to open, wrong password.", Toast.LENGTH_SHORT).show();}
        else if(obj.get_data_access_error_code()==0) {
            //vault open settings
            vault_open = true;
            deleteVaultDialog.dismiss();
            if (called_from == 1)
            {
                vaultFragment = (Vault_Fragment) getSupportFragmentManager().findFragmentByTag("vaultFragment");
                vaultFragment.set_open_close_vault_button_text("Close Vault");
                vaultFragment.enable_add_button(true);
                vaultFragment.enable_delete_vault_button(false);
                //pushing the data to recycler view
                vaultFragment.add_multiple_data_to_recyclerview(obj.get_vault_data());
            }
            vault_data_list.clear();
            vault_data_list.addAll(0,obj.get_vault_data());
            if(called_from==0)
            {   enter_new_password_dialog(0);}
        }
    }
    @Override
    public void close_vault()
    {
        vault_open=false;
        vaultFragment=(Vault_Fragment)getSupportFragmentManager().findFragmentByTag("vaultFragment");
        vaultFragment.set_open_close_vault_button_text("Open Vault");
        vaultFragment.enable_add_button(false);
        vaultFragment.enable_delete_vault_button(true);
        vault_data_list.clear();
        vaultFragment.empty_recycler_view();
        vault_db.close_vault();
        //remove data from recycler view
    }

    //recycler_view functions
    @Override
    public void copy_password_button_onclick(String copy_password)
    {
        ClipData clip=ClipData.newPlainText("copy_password",copy_password);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Password copied.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void copy_id_button_onclick(String copy_id)
    {
        ClipData clip=ClipData.newPlainText("copied_id",copy_id);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "ID copied.", Toast.LENGTH_SHORT).show();
    }
    private int data_id_1,position1;
    @Override
    public void delete_data_button_onclick(int data_id,int position)
    {
        data_id_1=data_id;position1=position;
        Map<String,Integer> map=get_color_id();
        String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
        materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Delete data..</font>"));
        materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">Do you really wan to delete the data?</font>"));
        materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

        materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                vaultFragment=(Vault_Fragment)getSupportFragmentManager().findFragmentByTag("vaultFragment");
                vaultFragment.remove_item_from_recycler_view(data_id_1,position1);
                for(int a=0;a<vault_data_list.size();a++)
                {
                    if(vault_data_list.get(a).id==data_id_1)
                    {
                        vault_data_list.remove(a);
                        break;
                    }
                }
                vault_db.delete_data_from_table(data_id_1);

                Toast.makeText(getApplicationContext(), "Data deleted.", Toast.LENGTH_SHORT).show();
            }
        });
        materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        materialAlertDialogBuilder.show();
    }

    //delete_vault_dialog functions
    public ArrayList<String> get_vault_names()
    {
        ArrayList<String> vault_name_list=new ArrayList();
        vault_name_list.add("Select vault name");
        for(int a=0;a<table_name_and_vault_name_list.size();a++)
        {   vault_name_list.add(table_name_and_vault_name_list.get(a)[1]); }
        return vault_name_list;
    }
    @Override
    public void delete_vault_dialog()//from fragment
    {
        delete_dialog_start=true;
        table_name_and_vault_name_list=vault_db.get_table_name_vault_names();
        deleteVaultDialog.called_from_option=1;
        deleteVaultDialog.show(getSupportFragmentManager(),"DeleteVaultDialog");
    }
    @Override
    public void delete_vault_dialog(int dialog_option,int error_code,int spinner_position,String password,boolean open_vault_dialog_start,boolean delete_dialog_start,int called_from)//from dialog
    {
        if(dialog_option==1)//cancel button
        {   deleteVaultDialog.dismiss();}
        else if(dialog_option==0)//ok button
        {
            if(error_code==1)
            {   Toast.makeText(getApplicationContext(),"Select the vault to delete.", Toast.LENGTH_SHORT).show();}
            else if(error_code==2)
            {   Toast.makeText(getApplicationContext(),"Enter vault password.", Toast.LENGTH_SHORT).show();}
            else if(error_code==0)
            {
                if(delete_dialog_start==true && open_vault_dialog_start==false)//delete vault dialog
                {
                    int delete_satatus = vault_db.delete_table(spinner_position - 1, password);
                    if (delete_satatus == 0) {
                        Toast.makeText(getApplicationContext(), "Vault " + table_name_and_vault_name_list.get(spinner_position - 1)[1] + " deleted.", Toast.LENGTH_SHORT).show();
                        deleteVaultDialog.dismiss();
                    } else if (delete_satatus == 1) {
                        Toast.makeText(getApplicationContext(), "Deletion failed, wrong password.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(delete_dialog_start==false && open_vault_dialog_start==true)//open vault dialog
                {   open_vault(spinner_position-1,password,called_from); }
            }
        }
    }

    //create_new_vault_dialog functions
    @Override
    public void create_new_vault_dialog()//from fragment
    {
        createVaultDialog=new create_vault_dialog();
        createVaultDialog.show(getSupportFragmentManager(),"CreateNewVaultDialog");
    }
    @Override
    public void create_new_vault_dialog(int option,int error_code,String vault_name,String password)//from dialog
    {
        if(option==1)//cancel button
        {
            createVaultDialog.dismiss();
        }
        else if(option==0)//ok button
        {
            if(error_code==0)
            {
                error_code=vault_db.create_table(vault_name,password);
                if(error_code==0)
                {
                    createVaultDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Vault "+vault_name+" successfully created.", Toast.LENGTH_SHORT).show();
                }
                else if(error_code==1)
                {   Toast.makeText(getApplicationContext(),"Vault named "+vault_name+" already present, please enter a new name.", Toast.LENGTH_SHORT).show();}
                else if(error_code==2)
                {}
            }
            else if(error_code==1)
            {
                Toast.makeText(getApplicationContext(),"Vault name is empty", Toast.LENGTH_SHORT).show();
            }
            else if(error_code==2)
            {
                Toast.makeText(getApplicationContext(),"Enter vault password.", Toast.LENGTH_SHORT).show();
            }
            else if(error_code==3)
            {
                Toast.makeText(getApplicationContext(),"Enter confirmation password", Toast.LENGTH_SHORT).show();
            }
            else if(error_code==4)
            {
                Toast.makeText(getApplicationContext(),"Password and confirmation password do not match", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //----------------------------------------------------------------------------------function for drawer window-----------------------------------------------------------------------------------------------------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        Map<String,Integer> map=get_color_id();
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        //drawer_layout.closeDrawer(GravityCompat.START);
        if(menu_item.getItemId()==R.id.generator_item)
        {
            fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment,new Key_Generator_Fragment(),"key_generator_fragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Password Generator" + "</font>"));
            current_fragment_code=1;
            drawer_layout.closeDrawers();
        }
        else if(menu_item.getItemId()==R.id.vault_item)
        {
            fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment,new Vault_Fragment(),"vaultFragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Local Vault" + "</font>"));
            current_fragment_code=2;
            drawer_layout.closeDrawers();
        }
        else if(menu_item.getItemId()==R.id.backup_item)
        {
            fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment,new Sync_Fragment(),"syncFragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Cloud Sync & Local Backup" + "</font>"));
            current_fragment_code=3;
            drawer_layout.closeDrawers();
        }
        else if(menu_item.getItemId()==R.id.about_item)
        {
            /*fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment, new About_Fragment(), "aboutFragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "About" + "</font>"));
            current_fragment_code=4;*/
            drawer_layout.closeDrawers();
            aboutDialog.show(getSupportFragmentManager(),"aboutDialog");
        }
        return true;
    }
}