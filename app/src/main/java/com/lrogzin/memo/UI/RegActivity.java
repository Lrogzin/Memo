package com.lrogzin.memo.UI;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lrogzin.memo.DB.UserDao;
import com.lrogzin.memo.R;
import com.lrogzin.memo.Util.EditTextClearTools;

public class RegActivity extends AppCompatActivity {
    private EditText userName,passWord,rePassword;
    private ImageView unameClear,pwdClear,repwdClear;
    private TextView userLogin;
    private Button register;
    private UserDao userdao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        userdao = new UserDao(this);
        init();
        ViewClick();
    }

    private void ViewClick() {

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin.setTextColor(Color.rgb(0, 0, 0));
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username=userName.getText().toString();
                final String password=passWord.getText().toString();
                final String repassword=rePassword.getText().toString();
                if(username.isEmpty()){
                    Toast.makeText(getApplicationContext(),"帐号不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(!password.equals(repassword)){
                    Toast.makeText(getApplicationContext(),"两次密码输入不一致",Toast.LENGTH_LONG).show();
                    return;
                }

                Cursor cursor = userdao.query(username.trim(), password.trim());
                if (cursor.moveToNext()) {
                    Toast.makeText(getApplicationContext(),"该用户已被注册，请重新输入",Toast.LENGTH_LONG).show();
                    userName.requestFocus();
                }else{
                    userdao.insertUser(username,password);
                    cursor.close();
                    Toast.makeText(getApplicationContext(),"用户注册成功，请前往登录",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
                }

            }
        });
    }

    private void init() {
        userName = (EditText) findViewById(R.id.et_userName);
        passWord = (EditText) findViewById(R.id.et_password);
        rePassword = (EditText) findViewById(R.id.et_repassword);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        repwdClear = (ImageView) findViewById(R.id.iv_repwdClear);
        userLogin = (TextView) findViewById(R.id.link_signup);
        register= (Button) findViewById(R.id.btn_login);
        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(passWord,pwdClear);
        EditTextClearTools.addClearListener(rePassword,repwdClear);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
    }
}
