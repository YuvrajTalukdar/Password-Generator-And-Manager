package com.yuvraj.passwordgeneratorandmanager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class File_Explorer_Activity extends AppCompatActivity implements new_folder_dialog.new_folder_dialog_listener{

    private TextView folder_name_text_view;
    private RecyclerView recycler_view;
    private EditText file_name;
    private recycler_view_adapter2 recycler_view_adapter2_obj;
    private ArrayList<directory_data_handler> directory_data_handlers_list=new ArrayList<>();
    private File root;
    private Uri uri;
    private DocumentFile curDocumentFile,rootDocumentFile;
    private Stack<DocumentFile> DocumentFile_stack=new Stack<>();
    private new_folder_dialog new_folder_dialog_obj;
    private int save_load_code;
    private SharedPreferences settings_reader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        color_scheme_changer(settings_reader.getInt("color_scheme_code", 1));
        setContentView(R.layout.activity_file_explorer);

        new_folder_dialog_obj=new new_folder_dialog();
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        file_name=findViewById(R.id.file_name);
        Button save_load_button=findViewById(R.id.save_load_button);
        String start_motive = getIntent().getStringExtra("file_explorer_intent_start_motive");

        Map<String,Integer> map=get_color_id();
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        if(start_motive.equals("load_file"))
        {
            save_load_button.setText(R.string.Load_Vault_File);
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Load Backup File" + "</font>"));
            save_load_code=0;
        }
        else if(start_motive.equals("backup_file"))
        {
            save_load_button.setText(R.string.Backup_Vaults);
            getSupportActionBar().setTitle(Html.fromHtml("<font color="+medium_color+">" + "Backup Data" + "</font>"));
            save_load_code=1;
        }
        save_load_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(save_load_code==0)//load vault file
                {
                    int error_code=load_backup_file(file_name.getText().toString());
                    if(error_code==0)
                    {   Toast.makeText(getApplicationContext(),"Data from backup file loaded successfully. ", Toast.LENGTH_LONG).show();finish();}
                    else if(error_code==1)
                    {   Toast.makeText(getApplicationContext(),"Please select a valid backup file.", Toast.LENGTH_LONG).show();}
                    else if(error_code==2)
                    {   Toast.makeText(getApplicationContext(),"Failed to load backup file.",Toast.LENGTH_LONG).show();}
                    else if(error_code==3)
                    {   Toast.makeText(getApplicationContext(),"File not found.",Toast.LENGTH_LONG).show();}
                }
                else if(save_load_code==1)//save vault file
                {
                    if(file_name.getText().toString().isEmpty())
                    {    Toast.makeText(getApplicationContext(),"Please enter a name.", Toast.LENGTH_LONG).show();}
                    else if(curDocumentFile.findFile(file_name.getText().toString()+".zip")!=null)
                    {    Toast.makeText(getApplicationContext(),file_name.getText().toString()+".zip is already present. Please enter a different name", Toast.LENGTH_LONG).show();}
                    else
                    {
                        backup_data_handler(file_name.getText().toString());
                        Toast.makeText(getApplicationContext(),"Local backup complete.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.DarkGrey));
        window.setNavigationBarColor(getResources().getColor(R.color.Black,null));

        ImageButton create_new_folder_button=findViewById(R.id.new_folder);
        create_new_folder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_folder_dialog_obj.show(getSupportFragmentManager(),"new_folder_dialog");
            }
        });

        ImageButton back_button=findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DocumentFile_stack.size()>1)
                {
                    DocumentFile_stack.pop();
                    curDocumentFile=DocumentFile_stack.lastElement();
                    empty_recycler_view();
                    add_multiple_data_to_recycle_view(curDocumentFile);
                }
            }
        });

        folder_name_text_view=findViewById(R.id.FolderName1);

        //boolean permission_got=requestForPermission();
        request_root_dir_uri();

        recycler_view=findViewById(R.id.file_explorer_recycler_view);
        recycler_view_adapter2_obj=new recycler_view_adapter2(this, directory_data_handlers_list,new recycler_view_adapter2.directory_data_handler_adapter_listener()
        {
            @Override
            public void file_folder_onclick(View v,int data_id,int position)
            {
                if(directory_data_handlers_list.get(position).is_folder)
                {
                    DocumentFile_stack.push(directory_data_handlers_list.get(position).file);
                    curDocumentFile=directory_data_handlers_list.get(position).file;
                    if(save_load_code==0)
                    {   file_name.setText("");}
                    empty_recycler_view();
                    add_multiple_data_to_recycle_view(curDocumentFile);
                }
                else if(!directory_data_handlers_list.get(position).is_folder && save_load_code==0)
                {   file_name.setText(directory_data_handlers_list.get(position).file.getName());}
            }

            @Override
            public void delete_file_folder(View v,int data_id,int position)
            {   delete_file_folder_on_click(data_id,position);}
        });
        recycler_view.setAdapter(recycler_view_adapter2_obj);
        LinearLayoutManager linear_layout=new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linear_layout);
        /*
        folder_stack.clear();
        if(permission_got)
        {   add_multiple_data_to_recycle_view(root);}*/
    }

    private void color_scheme_changer(int color_scheme_code)
    {
        if(color_scheme_code==0)
        {   setTheme(R.style.DarkRedTheme);}
        else if(color_scheme_code==1)
        {    setTheme(R.style.DarkGreenTheme);}
        else if(color_scheme_code==2)
        {   setTheme(R.style.DarkGreyTheme);}
        else if(color_scheme_code==3)
        {   setTheme(R.style.DarkBlueTheme);}
        else if(color_scheme_code==4)
        {   setTheme(R.style.DarkVioletTheme);}
        else if(color_scheme_code==5)
        {   setTheme(R.style.DarkPinkTheme);}
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

    private int load_backup_file(String backup_file_name)
    {
        int error_code=0;
        try
        {
            DocumentFile zip_file = curDocumentFile.findFile(backup_file_name);
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

    private String get_date_and_time()
    {
        int y= Calendar.getInstance().get(Calendar.YEAR);
        int m=Calendar.getInstance().get(Calendar.MONTH);
        int d=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        long t=Calendar.getInstance().getTime().getTime();
        System.out.println("date_time="+y+"/"+m+"/"+d+"_"+t);
        return y+"/"+m+"/"+d+"_"+t;
    }

    private void backup_data_handler(String zip_file_name)
    {
        //getting the meta data from the database and initializing the required database variables.
        database_handler vault_db=new database_handler(this,true);
        ArrayList<String[]> table_name_array_list=vault_db.get_table_name_vault_names();
        //compression of data
        try
        {
            curDocumentFile.createFile("",zip_file_name+".zip");
            DocumentFile zip_file=curDocumentFile.findFile(zip_file_name+".zip");
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
        }
        //clearing of data
        vault_db.close();
        table_name_array_list.clear();
    }

    private Map<String,Integer> get_color_id()
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

    void delete_file_folder_on_click(int data_id,int position)
    {
        Map<String,Integer> map=get_color_id();
        String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        String file_folder_message="",name="";
        if(directory_data_handlers_list.get(position).is_folder)
        {   file_folder_message="Folder";}
        else
        {   file_folder_message="File";}
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Delete "+file_folder_message+"..</font>"));
        materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">Are you sure you want to delete "+name+"?</font>"));
        materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

        materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean ok=false;
                String file_folder_message="",name="";
                if (directory_data_handlers_list.get(position).id == data_id)
                {
                    name=directory_data_handlers_list.get(position).file_folder_name;
                    if(directory_data_handlers_list.get(position).is_folder)
                    {   file_folder_message="Folder";}
                    else
                    {   file_folder_message="File";}
                    if (directory_data_handlers_list.get(position).file.delete())
                    {
                        directory_data_handlers_list.remove(position);
                        recycler_view_adapter2_obj.notifyItemRemoved(position);
                        ok=true;
                    }
                }
                if(ok==true)
                {   Toast.makeText(getApplicationContext(),file_folder_message+" "+name+" deleted.", Toast.LENGTH_LONG).show();}
                else
                {   Toast.makeText(getApplicationContext(),"Delete operation failed!", Toast.LENGTH_LONG).show();}
            }
        });
        materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        materialAlertDialogBuilder.show();
    }

    @Override
    public void on_new_folder_name_send(int option,String name)
    {
        if(option==0)
        {
            if(name.isEmpty())
            {   Toast.makeText(getApplicationContext(), "Please enter a folder name.", Toast.LENGTH_LONG).show();}
            else if(curDocumentFile.findFile(name)!=null)
            {   Toast.makeText(getApplicationContext(),"Folder with name '"+ name+"' already exist, please enter a different name.", Toast.LENGTH_LONG).show();}
            else
            {
                curDocumentFile.createDirectory(name);
                empty_recycler_view();
                add_multiple_data_to_recycle_view(curDocumentFile);
                new_folder_dialog_obj.dismiss();
            }
        }
        else if(option==1)
        {   new_folder_dialog_obj.dismiss();}
    }

    void request_root_dir_uri()
    {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,Uri.fromFile(root));
        startActivityForResult(intent,2);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        try {
            uri=data.getData();
            rootDocumentFile = DocumentFile.fromTreeUri(this, uri);
            curDocumentFile=rootDocumentFile;
            System.out.println("Got uri permission.");
            DocumentFile_stack.clear();
            DocumentFile_stack.push(rootDocumentFile);
            add_multiple_data_to_recycle_view(rootDocumentFile);
        }
        catch(Exception e)
        {
            System.out.println("Failed to get root dir uri permission.");
            e.printStackTrace();
            finish();
        }

    }

    public void empty_recycler_view()
    {
        directory_data_handlers_list.clear();
        recycler_view_adapter2_obj.notifyDataSetChanged();
    }

    private void add_multiple_data_to_recycle_view(DocumentFile f)
    {
        ListDir(f);
        LayoutAnimationController layoutAnimationController= AnimationUtils.loadLayoutAnimation(recycler_view.getContext(),R.anim.layout_fall_down);
        recycler_view.setLayoutAnimation(layoutAnimationController);
        recycler_view_adapter2_obj.notifyItemRangeInserted(0,directory_data_handlers_list.size());
        recycler_view.scheduleLayoutAnimation();
    }

    public boolean requestForPermission() {
        boolean permission_got=false;
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
            {
                //ListDir(curFile);
                permission_got=true;
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                finish();
                permission_got=false;
            }
        }
        return permission_got;
    }

    class Sortbyname implements Comparator<directory_data_handler>
    {
        public int compare(directory_data_handler d1, directory_data_handler d2)
        {   return d1.file_folder_name.compareTo(d2.file_folder_name);}
    }

    void ListDir(DocumentFile f)
    {
        curDocumentFile = f;
        DocumentFile[] files = f.listFiles();
        directory_data_handlers_list.clear();
        ArrayList<directory_data_handler> directory_data_handlers_list_for_files=new ArrayList<>();
        ArrayList<directory_data_handler> directory_data_handlers_list_for_folders=new ArrayList<>();
        int id=0;
        for (DocumentFile file : files)
        {
            if(!file.isDirectory())
            {
                if(save_load_code==1)
                {   directory_data_handlers_list_for_files.add(new directory_data_handler(id,file));}
                else if(save_load_code==0 && file.getName().endsWith(".zip"))
                {   directory_data_handlers_list_for_files.add(new directory_data_handler(id,file));}
            }
            else
            {   directory_data_handlers_list_for_folders.add(new directory_data_handler(id,file));}
            id++;
        }
        /*
        if(root.getPath().equals(curFile.getPath()))
        {   folder_name_text_view.setText("Storage");}
        else
        {   folder_name_text_view.setText(curFile.getName());}
        */
        folder_name_text_view.setText(curDocumentFile.getName());
        //directory_data_handlers_list_for_files.sort((directory_data_handler d1, directory_data_handler d2)->d1.file_folder_name.compareTo(d2.file_folder_name));//needs sdk 24
        Collections.sort(directory_data_handlers_list_for_files,new Sortbyname());
        Collections.sort(directory_data_handlers_list_for_folders,new Sortbyname());
        directory_data_handlers_list.addAll(0,directory_data_handlers_list_for_folders);
        directory_data_handlers_list.addAll(directory_data_handlers_list.size(),directory_data_handlers_list_for_files);
        directory_data_handlers_list_for_files.clear();
        directory_data_handlers_list_for_folders.clear();
    }

    public static class directory_data_handler
    {
        public int id=-1;
        public String file_folder_name,file_extension;
        public boolean is_folder;
        public DocumentFile file;
        public directory_data_handler(int id_,DocumentFile f)
        {
            id=id_;
            file_folder_name=f.getName();
            is_folder=f.isDirectory();
            file=f;
        }
    }
}