package com.sadam.sadamlibarary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import static com.sadam.sadamlibarary.Tools.getDeclaredSetMethod;
import static com.sadam.sadamlibarary.Tools.isLightColor;

public abstract class MyActivity extends AppCompatActivity {
    public static final byte  TAKE_PHOTO=1;
    private static final String TAG = MyActivity.class.getSimpleName();
    private static int rootViewVisibleHeight;
    private static final short SOFTKEYBOARDMINHEIGHT=200;

    public static void logE(String warning) {
        Log.d("",StaticUtils.getCodeInfo(new Throwable())+warning+"\n \n");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = getWindow().getDecorView();
        final Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        rootViewVisibleHeight = rect.height();
        /*监听视图树中全局布局发生改变或者视图中的某个视图的可视状态发生改变*/
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect1 = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect1);
                int rootViewVisibleHeight_aferReDraw = rect1.height();
                if(rootViewVisibleHeight==0){
                    rootViewVisibleHeight = rootViewVisibleHeight_aferReDraw;
                }else if(rootViewVisibleHeight==rootViewVisibleHeight_aferReDraw){}else if(rootViewVisibleHeight-rootViewVisibleHeight_aferReDraw>SOFTKEYBOARDMINHEIGHT){
                    /*如果视图显示高度变小超过了200，可看作是键盘显示了*/
                    onSoftKeyBoardPopUp();
                    rootViewVisibleHeight = rootViewVisibleHeight_aferReDraw;
                }else if(rootViewVisibleHeight_aferReDraw-rootViewVisibleHeight>SOFTKEYBOARDMINHEIGHT){
                    /*如果视图高度变大超过200,可看作键盘收起了*/
                    onSoftKeyBoardPutAway();
                    rootViewVisibleHeight = rootViewVisibleHeight_aferReDraw;
                }
            }
        });
    }

    /**
     * this method used to handle some event when the soft keyboard has been put away.
     * when you use it ,you can override the methods on your MainActivity and put some event in it.
     */
    public void onSoftKeyBoardPutAway() {
    }
    /**
     * this method used to handle some event when the soft keyboard has been pop-up.
     * when you use it ,you can override the methods on your MainActivity and put some event in it.
     */
    public void onSoftKeyBoardPopUp() {
    }

    public void SadamReplaceFragment(int containerLayout_id, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerLayout_id, fragment);
        transaction.commit();
    }

    public void logE(Class clazz, String warning) {
        Log.e(clazz.getSimpleName(), warning);
    }

    public void setStatusBarColor(int r_color_id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.setStatusBarColor(getColor(r_color_id));
            if (isLightColor(getColor(r_color_id))) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }
    }


    public class DataImmigrator {


        public int copyAndImmigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, int r_raw_db_sqlite_id,boolean justTryNotWrite) {
            return copyAndImmigrateDataToLitePal(resourse_databasename,resourse_tablename,resourse_database_version,target_modelClass,new String[]{},new String[]{},r_raw_db_sqlite_id,justTryNotWrite);
        }

        public int copyAndImmigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, String[] litepal_columns, int r_raw_db_sqlite_id,boolean justTryNotWrite) {
            return copyAndImmigrateDataToLitePal(resourse_databasename,resourse_tablename,resourse_database_version,target_modelClass,litepal_columns,new String[litepal_columns.length],r_raw_db_sqlite_id,justTryNotWrite);
        }
        public int copyAndImmigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, String[] litepal_columns,String [] sqlite_columns, int r_raw_db_sqlite_id,boolean justTryNotWrite) {
            int count = -1;
            if (copyDatabaseFileToTheDirectory(r_raw_db_sqlite_id)) {
                count = immigrateDataToLitePal(resourse_databasename, resourse_tablename, resourse_database_version, target_modelClass,litepal_columns, sqlite_columns,justTryNotWrite);
                return count;
            } else {
                return count;
            }
        }


        /**当不指定任何列对应键值对时 默认 认为按员命名格式映射
         * @param resourse_databasename
         * @param resourse_tablename
         * @param resourse_database_version
         * @param target_modelClass
         * @return
         */
        public int immigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass,boolean justTryNotWrite) {
            return this.immigrateDataToLitePal(resourse_databasename,resourse_tablename,resourse_database_version,target_modelClass,new String[]{},justTryNotWrite);
        }


        /**当只给出 litepal里的 变量 但没给出  所对应的原来的列时  认为 这些变量是被抛弃的  不会作为列来处理。
         * @param resourse_databasename
         * @param resourse_tablename
         * @param resourse_database_version
         * @param target_modelClass
         * @param litepal_colums
         * @return
         */
        public int immigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass,String[] litepal_colums,boolean justTryNotWrite) {
            return this.immigrateDataToLitePal(resourse_databasename,resourse_tablename,resourse_database_version,target_modelClass,litepal_colums,new String[litepal_colums.length],justTryNotWrite);
        }

        /**
         * 把普通的sqlite数据库转移到LitePal数据库的工具
         *
         * Note:    target__modelClass 类必须有  零参数构造函数
         *
         * @param resourse_databasename     普通数据库的“.db”文件名
         * @param resourse_tablename        普通数据库的 table 名
         * @param resourse_database_version 普通数据库的版本号
         * @param target_modelClass         LitePal数据库的映射模型类    必须有zero argument constructor
         * @param litepal_columns                 LitePal数据库映射模型里一些非字段变量 以及 与普通Sqlite数据库里不对口的数据
         * @return 返回成功转移的数据数量
         *
         *
         * 就是先把   LitePal_model_columns = { name , age  ,  sex  }   -->   Fields =  {  Field(name),  Field(age)  , Field( sex ) }
         *            sqlite_columns   = {  name   ,  年龄   , }
         *
         *            很容易发现 这里的    age 和  年龄  虽然指的是同一个事物，可是命名格式不一样 所以得   年龄 --> age
         *
         *            咱就指定那些 特定的 变量就ok    Lite_Pal_columns剩下的  按 原来的思路来： 把字段本生作为  原生 sqlite 里的 columns 取，
         *
         *            如果  不想给某些字段指定（比如  LitePal_model 里的某些变量 不是字段 而是  普通变量， 而且  这些变量在  原生sqlite里也没有
         *
         *            指定 两个列表
         *
         *                                  columnIndex_LitePal_model  =  { age ，sex }
         *                                      columnIndex_resourse  = { 年龄 , null }
         *
         *            进入到函数内部就先遍历成
         *
         *            Key = { Field (name) , Field(age) , Field(sex) }
         *          value = { name , 年龄  , null }
         *
         *
         *          一般 都          Fields.length  >=  lite_pal_mode_column_index.length        因为我人比较懒   自己动手制定的肯定比类里定义的要少  有一种情况就是，你有可能给每个字段都制定了，这时候时两个一样大。
         *
         *            所以           for   i  in   lite_pal_mode_column_index :
         *                                  Fields.
         *
         *                                  但是你还得给剩下的都会指定以下  名字  时  还得 遍历一遍
         *                    所以  遍历的时候就判断一下  再 赋值
         *
         *                           for (Field field:fields){
         *                              for  i   in  LitePal_column:
         *                               if( field.getName().equals(i)){
         *                                   Value[j] = sqlite_column_index[i.index]
         *                               }else{
         *                                   Vale[j]=field.getName()
         *                               }
         *                           }
         *
         *
         *                           !idea:    利用  HashMap  类
         *
         *                           先把    所有  Field  以 它们的   name  作为  它们的  key  添加到里面
         *
         *                                          HashMap.put(Field.getName(),Field.getName())
         *
         *                           再     for  i in  range  litepal_column.length:
         *
         *                                      HashMap.put( litePal_column[i] , sqlite_columns[i] );
         *
         *
         *                            为了避免  重复劳动   先  判断一下     fields.length ==  lite_pal_columns.length
         */
        public int immigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, String[] litepal_columns,String [] sqlite_columns,boolean justTryNotWrite) {
            if (litepal_columns.length!= sqlite_columns.length){
                try {
                    throw new Exception("litepal_columns  and sqlite_columns must be one to one correspondence.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MyActivity.this, resourse_databasename, null, resourse_database_version);
            SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getReadableDatabase();

            Cursor cursor = sqLiteDatabase.query(resourse_tablename, null, null, null, null, null, null, null);
            int countOfSourceData = cursor.getCount();
            logE("需要迁移的数据量有："+countOfSourceData);
            int lastcount = DataSupport.count(target_modelClass);
            Log.e(TAG, "原有的数据量：" + lastcount);
            if (lastcount == DataSupport.deleteAll(target_modelClass)) {
                Log.e(TAG, "初始化LitePal镜像数据库完成");
            } else {
                Log.e(TAG, "初始化LitePal镜像数据库失败");
            }
            Log.e(TAG, "开始迁移数据...........");
            Field[] fields = target_modelClass.getDeclaredFields();
            HashMap<String, String> hashMap = new HashMap<>();
            if (fields.length == litepal_columns.length) {
                for (int i = 0; i < litepal_columns.length; i++) {
                    hashMap.put(litepal_columns[i], sqlite_columns[i]);
                }
            } else {
                for (Field field : fields) {
                    hashMap.put(field.getName(), field.getName());
                }
                Log.e("","********************下面是被替换的列 或者 被抛弃的列***************************");
                for (int i = 0; i < litepal_columns.length; i++) {
                    Log.e("",hashMap.put(litepal_columns[i], sqlite_columns[i]));
                }
                Log.e("","********************************************************************************");
            }


            if (cursor.moveToFirst()) {
                Log.e(TAG, "MoveToFirst");
                Log.e(TAG, "FirstWord");
                do {
                    try {
                        DataSupport object = (DataSupport) target_modelClass.newInstance();
                        for (Field field : fields) {
                            String sqlite_columns_name = hashMap.get(field.getName());
                            if (sqlite_columns_name != null) {
                                Method method = getDeclaredSetMethod(object.getClass(),field);
                                Class<?> dataType = field.getType();
                                Log.e("filed",field.getName());
                                Log.e("field.type",dataType+"");
                                Log.e("method",method.getName());
                                int columnIndex = cursor.getColumnIndex(sqlite_columns_name);
                                Log.e("columnIndex",""+columnIndex);
                                Object data = null;
                                if (dataType.isAssignableFrom(String.class)) {
                                    data = cursor.getString(columnIndex);
                                } else if (dataType.isAssignableFrom(Integer.TYPE)||dataType.isAssignableFrom(Integer.class)) {
                                    data = cursor.getInt(columnIndex);
                                } else if(dataType.isAssignableFrom(Date.class)){
                                    data = new Date(cursor.getInt(columnIndex));
                                }  else if (dataType.isAssignableFrom(Long.TYPE)||dataType.isAssignableFrom(Long.class)) {
                                    data = cursor.getLong(columnIndex);
                                }   else {
                                    data = cursor.getBlob(columnIndex);
                                }
                                Log.e("data",data+"");

                                method.invoke(object, data);
                            } else {
                            }
                        }
                        if(justTryNotWrite){
                            if(object.save()){
                                System.out.println(" saved successfully.");
                            }else{
                                System.out.println("saved failed.");
                            }
                        }else{
                            System.out.println("just try not to save.");
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            int count = DataSupport.count(target_modelClass);
            Log.e(TAG, "总共迁移数据量：" + count+"  失败："+(countOfSourceData-count));
            return count;
        }

        /**
         * 复制数据库文件 从res.raw目录 --> 到App缓存目录的databases目录
         * Note:  res.raw里的数据库文件名  必须为  db.sqlite.db
         *
         * @return 返回复制是否成功，成功返回true，失败返回false
         */
        public boolean copyDatabaseFileToTheDirectory(int r_raw_db_sqlite_id) {
            final int BUFFER_SIZE = 200000;
//
            final String DB_NAME = "db_sqlite.db"; //保存的数据库文件名
            String dbfile = getDBFilePath(DB_NAME);
            logE(dbfile);
            String dbPath = dbfile.substring(0,dbfile.length()-DB_NAME.length());
            logE(dbPath);
            try {
                if (!(new File(dbfile).exists())) {//判断数据库文件是否存在，若不存在则执行导入
                    File filepath = new File(dbPath);
                    if (!filepath.exists()) {
                        filepath.mkdirs();
                    }
                    if (filepath.exists()) {
                        InputStream is = getResources().openRawResource(r_raw_db_sqlite_id); //欲导入的数据库
                        FileOutputStream fos = new FileOutputStream(dbfile);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count = 0;
                        while ((count = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, count);
                        }
                        fos.close();
                        is.close();
                    }
                } else {
                    Log.e(TAG, "之前的数据库文件还在");
                    return true;
                }
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        /**验证数据库文件是否存在
         * @param db_filename 在程序数据包文件夹里的databases文件夹里的数据库文件名
         * @return  如果文件存在返回 true ，否则 返回 false
         */
        public boolean isExistDataFile(String db_filename){
            return new File(getDBFilePath(db_filename)).exists();
        }

        /**删除litepal生成的三个数据库文件 xxx.db ,xxx.db-shm , xxx.db-wal
         * @param db_filename 是在项目的assets目录下litepal.xml文件里<dbmane></dbmane>标签里定义的value（Note：不含“.db")
         * @return  删除是否成功
         */
        public boolean dropLitePalDB(String db_filename){
              for(String name:new String[]{db_filename+".db",db_filename+".db-shm",db_filename+".db-wal"}){
                  dropDBFile(name);
              }
              return !isExistDataFile(db_filename+".db");
        }

        /**删除数据库文件
         * @param db_filename 在程序数据包文件夹里的databases文件夹里的数据库文件名
         * @return  是否删除成功
         */
        public boolean  dropDBFile(String db_filename){
            return new File(getDBFilePath(db_filename)).delete();
        }

        private String getDBFilePath(String db_filename){
            final String PACKAGE_NAME = getPackageName();
            final String DB_PATH = "/data"
                    + Environment.getDataDirectory().getAbsolutePath() + "/"
                    + PACKAGE_NAME;  //在手机里存放数据库的位置
            final String dbPath = DB_PATH + "/databases/";
            final String dbfile_absolutelyPath = dbPath + db_filename;
            return dbfile_absolutelyPath;
        }
    }
}
