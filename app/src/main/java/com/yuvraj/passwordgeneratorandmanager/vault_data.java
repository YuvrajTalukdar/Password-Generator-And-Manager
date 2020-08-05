package com.yuvraj.passwordgeneratorandmanager;

public class vault_data {
    public Integer id;
    public String account_type,account_id,account_password,date_of_modification,vault_name;
    public Integer is_meta_data;

    public vault_data(Integer id, String account_type, String account_id, String account_password, String date_of_modification,Integer IS_META_DATA,String vaultname) {
        this.id = id;
        this.account_type = account_type;
        this.account_id = account_id;
        this.account_password = account_password;
        this.date_of_modification = date_of_modification;
        this.is_meta_data=IS_META_DATA;
        this.vault_name=vaultname;
    }

    public vault_data()
    {}
}
