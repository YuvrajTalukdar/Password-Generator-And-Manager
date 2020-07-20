package com.yuvraj.passwordgeneratorandmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static java.sql.Types.NULL;

public class database_handler extends SQLiteOpenHelper {

    private static int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="Vaults.db";
    private boolean encryption;
    private static final String id="ID",account_type_name="ACCOUNT_TYPE_NAME",account_login_id="ACCOUNT_LOGIN_ID",account_password="PASSWORD",entry_date="ENTRY_DATE",is_meta_data="IS_META_DATA",vault_name="VAULT_NAME";
    private AES aes_handler;
    //table meta data holder
    private ArrayList<String[]> table_name_and_vault_name_list;
    private boolean table_metadata_updated=false;
    //vault_locking mechanism
    private String current_password="",current_table_name="",current_vault_name="";
    private boolean vault_open=false;

    public database_handler(@Nullable Context context,boolean encryption_enabled) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryption=encryption_enabled;
        table_name_and_vault_name_list=new ArrayList();
        if(encryption_enabled==true)
        {   aes_handler=new AES();}
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    private String create_random_table_name(int length,boolean c_letter,boolean s_letters,boolean numbers,boolean spl_char)//done and checked
    {
        StringBuffer string_buff = new StringBuffer();
        while (length != 0) {
            byte[] array = new byte[256];
            new Random().nextBytes(array);
            String random_string = new String(array, Charset.forName("UTF-8"));
            int a;
            for (a = 0; a < random_string.length(); a++) {

                char ch = random_string.charAt(a);

                if ((((ch >= 'a') && (ch <= 'z') && (s_letters == true))
                        || ((ch >= 'A') && (ch <= 'Z') && (c_letter == true))
                        || ((ch >= '0') && (ch <= '9') && (numbers == true))
                        || ((ch >= '!') && (ch <= '/') && (spl_char == true))
                )
                        && (length > 0)) {

                    string_buff.append(ch);
                    length--;
                }
            }
        }
        return string_buff.toString();
    }

    public int create_table(String tablename,String password)//done and checked
    {
        SQLiteDatabase db = getWritableDatabase();
        if (table_metadata_updated == false)
        {
            table_name_and_vault_name_list.clear();
            table_name_and_vault_name_list = get_table_name_vault_name_list(db);
            table_metadata_updated=true;
        }
        boolean table_already_found=false;
        for(int a=0;a<table_name_and_vault_name_list.size();a++)
        {
            if(table_name_and_vault_name_list.get(a)[1].equals(tablename)==true)
            {
                table_already_found=true;
                break;
            }
        }
        if(table_already_found==false)
        {
            try {
                String generated_table_name = create_random_table_name(10, true, false, false, false);
                String query1 = "CREATE TABLE IF NOT EXISTS " + generated_table_name + "(" + id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        account_type_name + " TEXT, " +
                        account_login_id + " TEXT, " +
                        account_password + " TEXT, " +
                        entry_date + " TEXT, " +
                        is_meta_data + " INTEGER, " +
                        vault_name + " TEXT " + ");";
                db.execSQL(query1);
                ContentValues values = new ContentValues();
                values.put(account_type_name, NULL);
                values.put(account_login_id, NULL);
                if (encryption == true) {
                    String encrypted_text;
                    encrypted_text = aes_handler.encrypt(password, password);
                    values.put(account_password, encrypted_text);
                } else {
                    values.put(account_password, NULL);
                }
                String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                values.put(entry_date, date);
                values.put(is_meta_data, 1);
                values.put(vault_name, tablename);
                db.insert(generated_table_name, null, values);
                System.out.println("Table created successfully.");
                db.close();
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Table creation failed! Please try again." + e.getCause());
                db.close();
                return 2;//unknown error
            }
        }
        else
        {
            return 1;//table name already present.
        }
    }

    private String get_particular_table_metadata(SQLiteDatabase db,String table_actual_name,String column_name)//done and checked
    {
        Cursor c=db.rawQuery("SELECT * FROM "+table_actual_name,null);
        c.moveToFirst();
        String data=new String();
        while(!c.isAfterLast())
        {
            if(c.getInt(c.getColumnIndex(id))==1)
            {
                data=c.getString(c.getColumnIndex(column_name));
                break;
            }
            c.moveToNext();
        }
        c.close();
        return data;
    }

    private ArrayList<String[]> get_table_name_vault_name_list(SQLiteDatabase db)//done and checked
    {
        table_name_and_vault_name_list.clear();
        Cursor c=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        c.moveToFirst();
        String table_and_vault_name[];
        while(!c.isAfterLast())
        {
            if(!c.getString(0).equals("sqlite_sequence") && !c.getString(0).equals("android_metadata"))
            {
                table_and_vault_name = new String[3];
                table_and_vault_name[0] = c.getString(0);
                System.out.println("table_name=" + table_and_vault_name[0]);
                table_and_vault_name[1] = get_particular_table_metadata(db, table_and_vault_name[0], vault_name);
                table_and_vault_name[2] = get_particular_table_metadata(db, table_and_vault_name[0], account_password);
                table_name_and_vault_name_list.add(table_and_vault_name);
            }
            c.moveToNext();
        }
        c.close();
        table_metadata_updated=true;
        return table_name_and_vault_name_list;
    }

    public ArrayList<String[]> get_table_name_vault_names()
    {
        table_name_and_vault_name_list.clear();
        SQLiteDatabase db = getWritableDatabase();
        table_name_and_vault_name_list=get_table_name_vault_name_list(db);
        db.close();
        table_metadata_updated=true;
        return table_name_and_vault_name_list;
    }

    public int delete_table(int table_index,String password)//done and checked
    {
        SQLiteDatabase db = getWritableDatabase();
        if(table_metadata_updated==false)
        {
            table_name_and_vault_name_list.clear();
            table_name_and_vault_name_list = get_table_name_vault_name_list(db);
            table_metadata_updated=true;
        }
        int return_code=-1;
        if(encryption==true)
        {
            if(aes_handler.decrypt(table_name_and_vault_name_list.get(table_index)[2],password).get_decryption_success_status()==true)
            {
                return_code=0;
                db.execSQL("DROP TABLE IF EXISTS " + table_name_and_vault_name_list.get(table_index)[0]);
            }
            else
            {   return_code=1;}
        }
        else
        {
            db.execSQL("DROP TABLE IF EXISTS " + table_name_and_vault_name_list.get(table_index)[0]);
            return_code=0;
        }
        db.close();
        table_metadata_updated=false;
        return return_code;
    }

    public vault_data_and_error_status get_data_from_table(int table_index,String table_name,String vaultname,String password)
    {
        SQLiteDatabase db = getWritableDatabase();
        vault_data_and_error_status vault_data_and_error_status_obj;
        if(!table_metadata_updated)
        {
            table_name_and_vault_name_list.clear();
            table_name_and_vault_name_list = get_table_name_vault_name_list(db);
            table_metadata_updated=true;
            vault_data_and_error_status_obj=new vault_data_and_error_status(1,null);
            db.close();
            return vault_data_and_error_status_obj;
        }
        else if(!table_name.equals(table_name_and_vault_name_list.get(table_index)[0]) || !vaultname.equals(table_name_and_vault_name_list.get(table_index)[1]))
        {
            table_name_and_vault_name_list.clear();
            table_name_and_vault_name_list = get_table_name_vault_name_list(db);
            table_metadata_updated=true;
            vault_data_and_error_status_obj=new vault_data_and_error_status(2,null);
            db.close();
            return vault_data_and_error_status_obj;
        }
        else
        {
            ArrayList<vault_data> vault_data_list = new ArrayList();
            String query = "SELECT * FROM " + table_name_and_vault_name_list.get(table_index)[0];
            Cursor c = db.rawQuery(query, null);
            c.moveToFirst();

            if(encryption)
            {
                if(aes_handler.decrypt(table_name_and_vault_name_list.get(table_index)[2], password).get_decryption_success_status())//decryption success
                {
                    c.moveToNext();
                    while(!c.isAfterLast())
                    {
                        vault_data vault_data_obj = new vault_data(c.getInt(c.getColumnIndex(id)),
                                aes_handler.decrypt(c.getString(c.getColumnIndex(account_type_name)),password).get_decrypted_data(),
                                aes_handler.decrypt(c.getString(c.getColumnIndex(account_login_id)),password).get_decrypted_data(),
                                aes_handler.decrypt(c.getString(c.getColumnIndex(account_password)),password).get_decrypted_data(),
                                c.getString(c.getColumnIndex(entry_date)),
                                c.getInt(c.getColumnIndex(is_meta_data)),
                                c.getString(c.getColumnIndex(vault_name)));
                        vault_data_list.add(vault_data_obj);
                        c.moveToNext();
                    }
                    vault_data_and_error_status_obj=new vault_data_and_error_status(0,vault_data_list);
                }
                else//decryption failed
                {
                    vault_data_and_error_status_obj=new vault_data_and_error_status(3,null);
                }
            }
            else
            {
                //get data
                while (!c.isAfterLast())
                {
                    vault_data vault_data_obj = new vault_data(c.getInt(c.getColumnIndex(id)),
                            c.getString(c.getColumnIndex(account_type_name)),
                            c.getString(c.getColumnIndex(account_login_id)),
                            c.getString(c.getColumnIndex(account_password)),
                            c.getString(c.getColumnIndex(entry_date)),
                            c.getInt(c.getColumnIndex(is_meta_data)),
                            c.getString(c.getColumnIndex(vault_name)));
                    vault_data_list.add(vault_data_obj);
                    c.moveToNext();
                }
                vault_data_and_error_status_obj=new vault_data_and_error_status(0,vault_data_list);
                c.close();
                db.close();
                return vault_data_and_error_status_obj;
            }

            if(vault_data_and_error_status_obj.get_data_access_error_code()==0)
            {
                current_table_name=table_name_and_vault_name_list.get(table_index)[0];
                current_vault_name=table_name_and_vault_name_list.get(table_index)[1];
                current_password=aes_handler.decrypt(table_name_and_vault_name_list.get(table_index)[2],password).get_decrypted_data();
                vault_open=true;
            }
            c.close();
            db.close();
            return vault_data_and_error_status_obj;
        }
    }

    public int add_data_to_table(String new_account_id,String type,String pass)
    {
        if(!vault_open)
        {   return 1;}
        else
        {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            if (encryption)
            {
                values.put(account_type_name,aes_handler.encrypt(type,current_password));
                values.put(account_login_id,aes_handler.encrypt(new_account_id,current_password));
                values.put(account_password, aes_handler.encrypt(pass,current_password));
            }
            else
            {
                values.put(account_password, current_password);
                values.put(account_login_id,id);
                values.put(account_password,current_password);
            }
            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            values.put(entry_date, date);
            values.put(is_meta_data, 0);
            values.put(vault_name, current_vault_name);
            db.insert(current_table_name, null, values);
            System.out.println("Data inserted successfully.");
            db.close();
            return 0;
        }
    }

    public vault_data get_last_entered_data()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + current_table_name, null);
        c.moveToLast();
        db.close();

        return new vault_data(c.getInt(c.getColumnIndex(id)),
            aes_handler.decrypt(c.getString(c.getColumnIndex(account_type_name)),current_password).get_decrypted_data(),
            aes_handler.decrypt(c.getString(c.getColumnIndex(account_login_id)),current_password).get_decrypted_data(),
            aes_handler.decrypt(c.getString(c.getColumnIndex(account_password)),current_password).get_decrypted_data(),
            c.getString(c.getColumnIndex(entry_date)),
            c.getInt(c.getColumnIndex(is_meta_data)),
            c.getString(c.getColumnIndex(vault_name)));
    }

    public int delete_data_from_table(int data_id)
    {
        if(vault_open)
        {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM "+current_table_name+" WHERE "+id+" = "+data_id);
            db.close();
            return 0;
        }
        else
        {   return 1;}
    }

    public void close_vault()
    {
        current_password=null;
        current_table_name=null;
        current_vault_name=null;
        vault_open=false;
    }

    public static class vault_data_and_error_status {

        private int vault_data_access_error_code;
        ArrayList<vault_data> vault_data;

        public ArrayList<vault_data> get_vault_data() {
            return vault_data;
        }

        public int get_data_access_error_code() {
            return vault_data_access_error_code;
        }

        public vault_data_and_error_status(int error_code, ArrayList<vault_data> data) {
            vault_data_access_error_code=error_code;
            vault_data=data;
        }

        public vault_data_and_error_status()
        {}
    }
}
