package com.yuvraj.passwordgeneratorandmanager;

import android.Manifest;
import android.content.Intent;
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
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

public class File_Explorer_Activity extends AppCompatActivity implements new_folder_dialog.new_folder_dialog_listener{

    private TextView folder_name_text_view;
    private RecyclerView recycler_view;
    private recycler_view_adapter2 recycler_view_adapter2_obj;
    private ArrayList<directory_data_handler> directory_data_handlers_list=new ArrayList<>();
    private File root;
    private Uri uri;
    private DocumentFile curDocumentFile,rootDocumentFile;
    private Stack<DocumentFile> DocumentFile_stack=new Stack<>();
    private new_folder_dialog new_folder_dialog_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);

        new_folder_dialog_obj=new new_folder_dialog();
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        Button save_load_button=findViewById(R.id.save_load_button);
        String start_motive = getIntent().getStringExtra("file_explorer_intent_start_motive");
        if(start_motive.equals("load_file"))
        {
            save_load_button.setText(R.string.Load_Vault_File);
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#5CEF1C\">" + "Load Backup File" + "</font>"));
        }
        else if(start_motive.equals("backup_file"))
        {
            save_load_button.setText(R.string.Backup_Vaults);
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#5CEF1C\">" + "Backup Data" + "</font>"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
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

        boolean permission_got=requestForPermission();
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
                    empty_recycler_view();
                    add_multiple_data_to_recycle_view(curDocumentFile);
                }
            }
        });
        recycler_view.setAdapter(recycler_view_adapter2_obj);
        LinearLayoutManager linear_layout=new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linear_layout);
        /*
        folder_stack.clear();
        if(permission_got)
        {   add_multiple_data_to_recycle_view(root);}*/
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
            {   directory_data_handlers_list_for_files.add(new directory_data_handler(id,file));}
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