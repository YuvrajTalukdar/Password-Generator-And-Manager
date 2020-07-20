package com.yuvraj.passwordgeneratorandmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               Key_Generator_Fragment.Key_Generator_Fragment_Listener,
                                                               Vault_Fragment.Vault_Fragment_Listener,
                                                               create_vault_dialog.create_vault_dialog_listener,
                                                               delete_vault_dialog.delete_vault_dialog_listener,
                                                               enter_new_pass_dialog.enter_new_pass_dialog_listener{

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
    private ArrayList<String[]> table_name_and_vault_name_list;
    private boolean vault_open=false;
    public boolean delete_dialog_start=false,open_vault_dialog_start=false;
    private Vault_Fragment vaultFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //main activity elements
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer_layout=findViewById(R.id.drawer_layout);
        navigation_view=findViewById(R.id.navigation_view);

        navigation_view.setNavigationItemSelectedListener(this);
        action_bar_toggle=new ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.open,R.string.close);
        drawer_layout.addDrawerListener(action_bar_toggle);
        navigation_view.setItemIconTintList(null);//for default colour icons
        action_bar_toggle.setDrawerIndicatorEnabled(true);
        action_bar_toggle.syncState();
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#5CEF1C\">" + "Password Generator" + "</font>"));
        deleteVaultDialog=new delete_vault_dialog();
        //load default fragment
        fragment_manager=getSupportFragmentManager();
        fragment_transaction=fragment_manager.beginTransaction();
        fragment_transaction.add(R.id.container_fragment,new Key_Generator_Fragment());
        fragment_transaction.commit();

        //main activity functions
        clipboard=(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        vault_db=new database_handler(this,true);
        vault_data_list=new ArrayList<>();
    }

    //Key_Generator_fragment interface functions
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
                final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
                materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color='#5CEF1C'>Vault not opened..</font>"));
                materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color='#FF0000'>You need to open a vault to save the generated password.</font>"));
                materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

                materialAlertDialogBuilder.setPositiveButton("Open Vault", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        open_vault_dialog(0);
                    }
                });
                materialAlertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
    //Vault_Fragment interface functions
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

    public ArrayList<vault_data> get_recycler_view_data()
    {   return vault_data_list;}

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
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
        materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color='#5CEF1C'>Delete data..</font>"));
        materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color='#FF0000'>Do you really wan to delete the data?</font>"));
        materialAlertDialogBuilder.setBackground(getDrawable(R.drawable.grey_background));

        materialAlertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        materialAlertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
    //function for drawer window
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menu_item) {
        drawer_layout.closeDrawer(GravityCompat.START);
        if(menu_item.getItemId()==R.id.generator_item)
        {
            fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment,new Key_Generator_Fragment(),"key_generator_fragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#5CEF1C\">" + "Password Generator" + "</font>"));
            //drawer_layout.closeDrawers();
        }
        else if(menu_item.getItemId()==R.id.vault_item)
        {
            fragment_manager=getSupportFragmentManager();
            fragment_manager.beginTransaction().replace(R.id.container_fragment,new Vault_Fragment(),"vaultFragment").commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#5CEF1C\">" + "Local Vault" + "</font>"));
        }
        else if(menu_item.getItemId()==R.id.backup_item)
        {

        }
        else if(menu_item.getItemId()==R.id.about_item)
        {

        }
        return true;
    }
}