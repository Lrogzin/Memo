package com.lrogzin.memo.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lrogzin.memo.DB.UserDao;
import com.lrogzin.memo.R;
import com.lrogzin.memo.Util.EditTextClearTools;
import com.lrogzin.memo.Util.PermissionsUtil;

import de.hdodenhof.circleimageview.CircleImageView;


public class LoginActivity extends AppCompatActivity {

    private EditText userName,passWord;
    private ImageView unameClear,pwdClear;
    private TextView touristLogin,userReg;
    private CheckBox rememberPw,autoLogin;
    private CircleImageView iv_icon;
    private Button login;
    private UserDao userdao;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PermissionsUtil.checkAndRequestPermissions(this);
        init();//初始化组件
        loadSP();//读取SharedPreferences数据
        ViewClick();//注册组件点击事件
    }
    private void init(){
        userName = (EditText) findViewById(R.id.et_userName);
        passWord = (EditText) findViewById(R.id.et_password);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        iv_icon= (CircleImageView) findViewById(R.id.iv_icon);
        touristLogin = (TextView) findViewById(R.id.link_tourist);
        userReg = (TextView) findViewById(R.id.link_signup);
        login= (Button) findViewById(R.id.btn_login);
        rememberPw= (CheckBox) findViewById(R.id.remember_pw);
        autoLogin= (CheckBox) findViewById(R.id.auto_login);
        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(passWord,pwdClear);

    }

    private void loadSP() {
        sp = getSharedPreferences("userInfo", 0);
        ed = sp.edit();
        boolean remember_Pw =sp.getBoolean("REMEMBER_PW", false);
        boolean auto_Login =sp.getBoolean("AUTO_LOGIN", false);
        rememberPw.setChecked(remember_Pw);
        autoLogin.setChecked(auto_Login);
        String names=sp.getString("USER_NAME","");
        String passwords=sp.getString("PASSWORD", "");
        if(remember_Pw){
            userName.setText(names);
            passWord.setText(passwords);
            Bitmap bt = BitmapFactory.decodeFile("/sdcard/Memo/" + names + "head.jpg");// 从SD卡中找头像，转换成Bitmap
            if (bt != null) {
                @SuppressWarnings("deprecation")
                Drawable drawable = new BitmapDrawable(bt);// 转换成drawable
                iv_icon.setImageDrawable(drawable);
            } else {
                //如果SD里面没有就使用默认头像
            }
        }
        Intent intent=getIntent();
        if(intent.getStringExtra("code")!=null){
            if(intent.getStringExtra("code").equals("relogin")){
                auto_Login=false;
            }
        }

        if(auto_Login){
            login();
        }
    }

    private void login() {
        userdao = new UserDao(LoginActivity.this);
        final String username=userName.getText().toString();
        final String password=passWord.getText().toString();

        if(username.isEmpty()){
            Toast.makeText(getApplicationContext(),"帐号不能为空",Toast.LENGTH_LONG).show();
            return;
        }else if(password.isEmpty()){
            Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_LONG).show();
            return;
        }

        Cursor cursor = userdao.query(username.trim(), password.trim());
        if (cursor.moveToNext()) {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this,MainActivity.class);
            intent.putExtra("login_user",username);

            cursor.close();
            if(rememberPw.isChecked()){
                ed.putString("USER_NAME", username);
                ed.putString("PASSWORD", password);
                ed.putBoolean("REMEMBER_PW",true);
                ed.commit();
            }
            if(autoLogin.isChecked()){
                ed.putBoolean("AUTO_LOGIN",true);
                ed.commit();
            }
            startActivity(intent);
            finish();
        }else{
//            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
//            builder.setMessage("找不到该用户或者密码错误，是否注册该用户?");
//            builder.setTitle("是否注册");
//            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    userdao.insertUser(username,password);
//                    Toast.makeText(LoginActivity.this, "注册成功，请尝试登录", Toast.LENGTH_SHORT).show();
//                }
//            });
//            builder.setNegativeButton("取消",null);
//            builder.create().show();
            Toast.makeText(LoginActivity.this, "密码验证失败，请重新验证登录", Toast.LENGTH_SHORT).show();
        }

    }

    private void goReg() {
        userReg.setTextColor(Color.rgb(0, 0, 0));
        Intent intent = new Intent(getApplicationContext(), RegActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void ViewClick() {

        userReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goReg();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        touristLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this,MainActivity.class);
                intent.putExtra("login_user","tourist");
                startActivity(intent);
            }
        });

        autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(autoLogin.isChecked()){
                    rememberPw.setChecked(true);
                }
                ed.putBoolean("AUTO_LOGIN",autoLogin.isChecked());
                ed.commit();
            }
        });

        rememberPw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ed.putBoolean("REMEMBER_PW",rememberPw.isChecked());
                ed.commit();
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            System.exit(0);
            return;
        }
        else { Toast.makeText(getBaseContext(), "再按一次返回退出程序", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

}
