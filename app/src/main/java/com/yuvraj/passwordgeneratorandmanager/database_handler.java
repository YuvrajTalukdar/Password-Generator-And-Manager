package com.yuvraj.passwordgeneratorandmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

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
        return string_buff.toString()+"_"+create_unique_id_based_on_time();
    }

    private String create_unique_id_based_on_time()
    {
        int y=Calendar.getInstance().get(Calendar.YEAR);
        int m=Calendar.getInstance().get(Calendar.MONTH);
        int d=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        long t=Calendar.getInstance().getTime().getTime();
        System.out.println("date_time="+y+"_"+m+"_"+d+"_"+t);
        return y+"_"+m+"_"+d+"_"+t;
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
        Cursor c=db.rawQuery("SELECT * FROM "+table_actual_name+" WHERE "+id+" = (SELECT MIN("+id+") FROM "+table_actual_name+")",null);
        c.moveToFirst();
        String data=new String();
        while(!c.isAfterLast())
        {
            data=c.getString(c.getColumnIndex(column_name));
            break;
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

    private void create_table(String table_name,vault_data meta_data,SQLiteDatabase db)//used for restoring data
    {
        String query1 = "CREATE TABLE IF NOT EXISTS " + table_name + "(" + id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
        values.put(account_password, meta_data.account_password);
        values.put(entry_date, meta_data.date_of_modification);
        values.put(is_meta_data, 1);
        values.put(vault_name, meta_data.vault_name);
        db.insert(table_name, null, values);
    }

    public void restore_backup(ArrayList<vault_data_and_error_status> vault_list)
    {
        ContentValues values = new ContentValues();
        get_table_name_vault_names();
        SQLiteDatabase db = getWritableDatabase();
        boolean table_already_present;
        String currentTableName="",currentVaultName="";
        for (int a = 0; a < vault_list.size(); a++)
        {
            table_already_present=false;
            int index=-1;
            for(int b=0;b<table_name_and_vault_name_list.size();b++)
            {
                //checking if the particular table is already present or not.
                if(table_name_and_vault_name_list.get(b)[0].equals(vault_list.get(a).table_name))
                {
                    table_already_present=true;
                    index=b;
                    currentVaultName=table_name_and_vault_name_list.get(b)[1];
                    currentTableName=table_name_and_vault_name_list.get(b)[0];
                    break;
                }
                //checking is more some other table has the same vault name as that of the current one.
                if(table_name_and_vault_name_list.get(b)[1].equals(vault_list.get(a).get_vault_data().get(0).vault_name))
                {
                    for(int c=0;c<vault_list.get(a).get_vault_data().size();c++)
                    {   vault_list.get(a).get_vault_data().get(c).vault_name= vault_list.get(a).get_vault_data().get(c).vault_name+"_"+create_unique_id_based_on_time();}
                }
            }
            if(!table_already_present)
            {
                currentTableName=vault_list.get(a).table_name;
                create_table(currentTableName,vault_list.get(a).get_vault_data().get(0),db);
            }
            else//remove the duplicate data
            {
                //vault_data_and_error_status temp_data=get_raw_data_from_table_obj(vault_list.get(a).table_name);//encryption switch mechanism can be used here to reuse the function get_data_from_table.
                boolean changed=false;
                if(encryption)
                {   encryption=false;changed=true;}
                vault_data_and_error_status temp_data=get_data_from_table(index,currentTableName,currentVaultName,null);
                db = getWritableDatabase();//the above line closes the database so this is required.
                if(changed)
                {   changed=false;encryption=true;}
                if(temp_data.get_data_access_error_code()!=0)
                {
                    System.out.println("Vault loading ERROR! Failed to retrieve data from internal database.");
                    break;
                }
                for(int b=temp_data.get_vault_data().size()-1;b>=0;b--)
                {
                    for (int c = vault_list.get(a).get_vault_data().size() - 1;c >= 0; c--)
                    {
                        if (temp_data.get_vault_data().get(b).id==vault_list.get(a).get_vault_data().get(c).id)
                        {   vault_list.get(a).delete_data_using_index(c);}
                    }
                }
            }
            for (int b = 1; b < vault_list.get(a).get_vault_data().size(); b++)
            {
                values.put(account_type_name, vault_list.get(a).get_vault_data().get(b).account_type);
                values.put(account_login_id, vault_list.get(a).get_vault_data().get(b).account_id);
                values.put(account_password, vault_list.get(a).get_vault_data().get(b).account_password);
                values.put(entry_date,vault_list.get(a).get_vault_data().get(b).date_of_modification);
                values.put(is_meta_data, vault_list.get(a).get_vault_data().get(b).is_meta_data);
                values.put(vault_name, vault_list.get(a).get_vault_data().get(b).vault_name);
                db.insert(currentTableName,null,values);
                values.clear();
            }
        }
        db.close();
    }

    private vault_data_and_error_status get_raw_data_from_table_obj(String actual_table_name)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + actual_table_name;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        ArrayList<vault_data> vault_data_list=new ArrayList<>();
        while(!c.isAfterLast())
        {
            vault_data new_vault_data=new vault_data(c.getInt(c.getColumnIndex(id)),
                    c.getString(c.getColumnIndex(account_type_name)),
                    c.getString(c.getColumnIndex(account_login_id)),
                    c.getString(c.getColumnIndex(account_password)),
                    c.getString(c.getColumnIndex(entry_date)),
                    c.getInt(c.getColumnIndex(is_meta_data)),
                    c.getString(c.getColumnIndex(vault_name)));
            vault_data_list.add(new_vault_data);
            c.moveToNext();
        }
        vault_data_and_error_status obj=new vault_data_and_error_status(0,vault_data_list);
        db.close();
        return obj;
    }

    public String get_raw_data_from_table_string(String actual_table_name)//used for data backup
    {
        SQLiteDatabase db = getWritableDatabase();
        String data="";
        String query = "SELECT * FROM " + actual_table_name;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String header=id+","+account_type_name+","+account_login_id+","+account_password+","+entry_date+","+is_meta_data+","+vault_name+",\n";
        while(!c.isAfterLast())
        {
            String data1=c.getInt(c.getColumnIndex(id))+","+
                          c.getString(c.getColumnIndex(account_type_name))+","+
                          c.getString(c.getColumnIndex(account_login_id))+","+
                          c.getString(c.getColumnIndex(account_password))+","+
                          c.getString(c.getColumnIndex(entry_date))+","+
                          c.getInt(c.getColumnIndex(is_meta_data))+","+
                          c.getString(c.getColumnIndex(vault_name));
            data1=data1.replace("\n","").replace("\r","");
            data1=data1+",\n";
            data=data+data1;
            c.moveToNext();
        }
        data=header+data;
        db.close();
        return data;
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
        public ArrayList<vault_data> vault_data;
        public String table_name;

        public ArrayList<vault_data> get_vault_data() {
            return vault_data;
        }

        public void delete_data_using_index(int index)
        {   vault_data.remove(index);}

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

    public Task<Integer> change_vault_password(int id,String table_name,String encrypted_pass, String current_pass, String new_pass)
    {
        return Tasks.call(mExecutor, () ->{
            //check if password is correct
            if(!aes_handler.decrypt(encrypted_pass,current_pass).get_decryption_success_status())
            {   return 1;}
            //getting the current data
            vault_data_and_error_status data;
            data=get_raw_data_from_table_obj(table_name);
            //deleting the data from the table
            SQLiteDatabase db =getWritableDatabase();
            db.execSQL("DELETE FROM "+table_name);
            // modify te encrypted data
            ContentValues values = new ContentValues();
            for(int a=0;a<data.vault_data.size();a++)
            {
                if(a==0)
                {
                    values.put(account_type_name, data.vault_data.get(a).account_type);
                    values.put(account_login_id, data.vault_data.get(a).account_id);
                }
                else
                {
                    values.put(account_type_name,aes_handler.encrypt(aes_handler.decrypt(data.vault_data.get(a).account_type,current_pass).get_decrypted_data(),new_pass));
                    values.put(account_login_id,aes_handler.encrypt(aes_handler.decrypt(data.vault_data.get(a).account_id,current_pass).get_decrypted_data(),new_pass));
                }
                values.put(account_password,aes_handler.encrypt(aes_handler.decrypt(data.vault_data.get(a).account_password,current_pass).get_decrypted_data(),new_pass));

                values.put(entry_date,data.vault_data.get(a).date_of_modification);
                values.put(is_meta_data,data.vault_data.get(a).is_meta_data);
                values.put(vault_name, data.vault_data.get(a).vault_name);
                db.insert(table_name,null,values);
                values.clear();
            }
            data.vault_data.clear();
            db.close();
            //add the data to the data base

            close_vault();
            return 0;
        });
    }
}
