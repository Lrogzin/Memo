package com.lrogzin.memo.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lrogzin.memo.Adapter.NoteListAdapter;
import com.lrogzin.memo.Bean.NoteBean;
import com.lrogzin.memo.DB.NoteDao;
import com.lrogzin.memo.DB.UserDao;
import com.lrogzin.memo.R;
import com.lrogzin.memo.Util.EditTextClearTools;
import com.lrogzin.memo.Util.SpacesItemDecoration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {
    private UserDao userDao;
    private NoteDao noteDao;
    private RecyclerView rv_list_main;
    private NoteListAdapter mNoteListAdapter;
    private List<NoteBean> noteList;
    private String login_user;
    private TextView utv;
    private int nav_selected;
    private NavigationView navigationView;
    private Menu menuNav;
    private CircleImageView iv_user;
    private CircleImageView userPic;
    private Bitmap head;// 头像Bitmap
    private Bitmap loadhead;
    private static String path = "/sdcard/Memo/";// sd路径
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nav_selected=2;
        noteDao = new NoteDao(this);
        userDao = new UserDao(this);

        //Intent获取当前登录用户
        Intent intent = getIntent();
        login_user = intent.getStringExtra("login_user");
        setTitle("备忘录");
        utv= (TextView) findViewById(R.id.tv_loginuser);

        initData();
        initView();
        registerForContextMenu(rv_list_main);
        setCount();

    }
    //获取当前用户头像
    private Drawable getUserDrawable() {
        loadhead = BitmapFactory.decodeFile(path + login_user + "head.jpg");// 从SD卡中找头像，转换成Bitmap
        if (loadhead != null) {
            @SuppressWarnings("deprecation")
            Drawable drawable = new BitmapDrawable(loadhead);// 转换成drawable
            return drawable;
        }else{
            return getDrawable(R.mipmap.ic_logo);
        }
    }

    //设置抽屉菜单是否完成备忘录的数量
    private void setCount() {
        menuNav = navigationView.getMenu();
        int unfinishNum=noteDao.countType(login_user,0);//未完成备忘录
        int finishNum=noteDao.countType(login_user,1);//已完成备忘录
        int allNum = finishNum+unfinishNum;//所有备忘录
        MenuItem nav_all = menuNav.findItem(R.id.nav_all);
        MenuItem nav_finish = menuNav.findItem(R.id.nav_finish);
        MenuItem nav_unfinish = menuNav.findItem(R.id.nav_unfinish);

        String all_before = "所有备忘录";
        String finish_before = "已完成备忘录";
        String unfinish_before = "未完成备忘录";

        nav_all.setTitle(setSpanTittle(all_before,allNum));
        nav_finish.setTitle(setSpanTittle(finish_before,finishNum));
        nav_unfinish.setTitle(setSpanTittle(unfinish_before,unfinishNum));

    }

    //设置抽屉菜单是否完成备忘录数量的文字样式
    private SpannableString setSpanTittle(String tittle,int num){
        String tittle2=tittle+ "      "+num+"  ";
        SpannableString sColored = new SpannableString( tittle2 );
        sColored.setSpan(new BackgroundColorSpan( Color.GRAY ), tittle2.length()-(num+"").length()-4, tittle2.length(), 0);
        sColored.setSpan(new ForegroundColorSpan( Color.WHITE ), tittle2.length()-(num+"").length()-4, tittle2.length(), 0);
        return sColored;
    }

    //刷新数据库数据，其实对notelist单一更新即可，不必重新获取，但是偷懒了
    private void refreshNoteList(int mark){//mark--0=查询未完成，1=查询已完成，>1=查询所有
        noteList = noteDao.queryNotesAll(login_user,mark);
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
        setCount();
    }

    //初始化数据库数据
    private void initData() {
        Cursor cursor=noteDao.getAllData(login_user);
        noteList = new ArrayList<>();
        if(cursor!=null){
            while(cursor.moveToNext()){
                NoteBean bean = new NoteBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex("note_id")));
                bean.setTitle(cursor.getString(cursor.getColumnIndex("note_tittle")));
                bean.setContent(cursor.getString(cursor.getColumnIndex("note_content")));
                bean.setType(cursor.getString(cursor.getColumnIndex("note_type")));
                bean.setMark(cursor.getInt(cursor.getColumnIndex("note_mark")));
                bean.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
                bean.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
                bean.setOwner(cursor.getString(cursor.getColumnIndex("note_owner")));
                noteList.add(bean);
            }
        }
        cursor.close();

    }

    //初始化控件
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("flag", 0);
                intent.putExtra("login_user",login_user);
                startActivity(intent);
            }
        });

        //抽屉式菜单
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        utv = (TextView)headerLayout.findViewById(R.id.tv_loginuser);
        iv_user= (CircleImageView) headerLayout.findViewById(R.id.iv_user);
        utv.setText(login_user);
        iv_user.setImageDrawable(getUserDrawable());


        //设置RecyclerView的属性
        rv_list_main = (RecyclerView) findViewById(R.id.rv_list_main);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        rv_list_main.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        mNoteListAdapter = new NoteListAdapter();
        mNoteListAdapter.setmNotes(noteList);
        rv_list_main.setAdapter(mNoteListAdapter);

        //RecyclerViewItem单击事件
        mNoteListAdapter.setOnItemClickListener(new NoteListAdapter.OnRecyclerViewItemClickListener(){

            @Override
            public void onItemClick(View view, NoteBean note) {
                //Toast.makeText(MainActivity.this,""+note.getId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    //抽屉菜单事件
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            nav_selected=2;
            refreshNoteList(2);
        } else if (id == R.id.nav_finish) {
            nav_selected=1;
            refreshNoteList(1);

        } else if (id == R.id.nav_unfinish) {
            nav_selected=0;
            refreshNoteList(0);

        } else if (id == R.id.nav_edit_user) {
            if(login_user.equals("tourist")){
                Toast.makeText(getApplicationContext(),"游客不能更改用户信息，请注册一个有效账户再进行此操作",Toast.LENGTH_LONG).show();
            }else{
                showUserInfo();
            }


        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("确定退出当前用户？");
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("code","relogin");
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("取消", null);
            builder.create().show();

        } else if (id == R.id.nav_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("确定退出备忘录？");
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            builder.setNegativeButton("取消", null);
            builder.create().show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //用户信息修改窗口事件
    private void showUserInfo() {
        head=null;
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View viewDialog=inflater.inflate(R.layout.user_info_main,null);
        TextView username= (TextView) viewDialog.findViewById(R.id.tv_username);
        final EditText oldpwd= (EditText) viewDialog.findViewById(R.id.et_password);
        final EditText newpwd= (EditText) viewDialog.findViewById(R.id.et_newpassword);
        final EditText newrepwd=(EditText) viewDialog.findViewById(R.id.et_repassword);
        Button button= (Button) viewDialog.findViewById(R.id.btn_update);
        ImageView oldpwdClear = (ImageView) viewDialog.findViewById(R.id.iv_pwdClear);
        ImageView newpwdClear = (ImageView) viewDialog.findViewById(R.id.iv_newpwdClear);
        ImageView repwdClear = (ImageView) viewDialog.findViewById(R.id.iv_repwdClear);
        userPic = (CircleImageView) viewDialog.findViewById(R.id.iv_userpic);
        EditTextClearTools.addClearListener(oldpwd,oldpwdClear);
        EditTextClearTools.addClearListener(newpwd,newpwdClear);
        EditTextClearTools.addClearListener(newrepwd,repwdClear);
        username.setText(login_user);
        userPic.setImageDrawable(getUserDrawable());

        builder.setView(viewDialog);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(oldpwd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"原密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(newpwd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"新密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(newrepwd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"新密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(!newrepwd.getText().toString().equals(newpwd.getText().toString())){
                    Toast.makeText(getApplicationContext(),"两次密码输入不一致",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    int i;
                    i=userDao.updatePw(login_user,oldpwd.getText().toString(),newpwd.getText().toString());
                    if(i>0){
                        Toast.makeText(getApplicationContext(),"修改成功！请重新登录！",Toast.LENGTH_LONG).show();
                        savePicToSDCard(head);// 保存在SD卡中
                        iv_user.setImageDrawable(getUserDrawable());
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.putExtra("code","relogin");
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"密码校验失败，请重新输入！",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, userPic);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.main, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(MainActivity.this);
                //显示(这一行代码不要忘记了)
                popup.show();

            }
        });
        builder.create().show();
    }

    //头像更换弹出式菜单，选择拍照和使用图库
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.gallery:
//                Toast.makeText(this, "图库", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
                break;
            case R.id.camera:
//                Toast.makeText(this, "相机", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "head.jpg")));
                startActivityForResult(intent2, 2);// 采用ForResult打开
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNoteList(nav_selected);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        int position = -1;
        try {
            position = mNoteListAdapter.getPosition();
        } catch (Exception e) {

            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()){
            case Menu.FIRST+1://查看该笔记
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", noteList.get(position));
                intent.putExtra("data", bundle);
                startActivity(intent);
                break;

            case Menu.FIRST+2://编辑该笔记
                Intent intent2 = new Intent(MainActivity.this, EditActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("note", noteList.get(position));
                intent2.putExtra("data", bundle2);
                intent2.putExtra("flag", 1);//编辑笔记
                startActivity(intent2);
                break;

            case Menu.FIRST+3://删除该笔记
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                final int finalPosition = position;
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = noteDao.DeleteNote(noteList.get(finalPosition).getId());
                        if (ret > 0){
                            Toast.makeText(MainActivity.this,"删除成功", Toast.LENGTH_SHORT).show();
                            refreshNoteList(nav_selected);
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                break;

            case Menu.FIRST+4://标记为已完成
                NoteBean bean=noteList.get(position);
                if(bean.getMark()==1){
                    Toast.makeText(MainActivity.this,"它早就被完成了啊", Toast.LENGTH_SHORT).show();
                }else{
                    bean.setMark(1);
                    noteDao.updateNote(bean);
                    //noteList.get(position).setMark(1);
                    refreshNoteList(nav_selected);
                    mNoteListAdapter.notifyItemRangeChanged(position,position);
                }
                break;


            case Menu.FIRST+5://标记为未完成
                NoteBean bean2=noteList.get(position);
                if(bean2.getMark()==0){
                    Toast.makeText(MainActivity.this,"它本来就没完成啊", Toast.LENGTH_SHORT).show();
                }else{
                    bean2.setMark(0);
                    noteDao.updateNote(bean2);
                    //noteList.get(position).setMark(0);
                    refreshNoteList(nav_selected);
                    mNoteListAdapter.notifyItemRangeChanged(position,position);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {

//                        savePicToSDCard(head);// 保存在SD卡中
                        userPic.setImageBitmap(head);// 用ImageView显示出来
                    }
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //调用系统的裁剪功能的实现
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    //保存图片到SD卡
    private void savePicToSDCard(Bitmap mBitmap) {
        if(mBitmap!=null){
            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                return;
            }
            FileOutputStream b = null;
            File file = new File(path);
            file.mkdirs();// 创建文件夹
            String fileName = path + login_user+"head.jpg";// 图片名字
            try {
                b = new FileOutputStream(fileName);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    // 关闭流
                    b.flush();
                    b.close();
                } catch (IOException e) {

                }
            }
        }else{

        }

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

