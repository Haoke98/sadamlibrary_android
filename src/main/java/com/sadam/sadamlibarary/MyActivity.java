package com.sadam.sadamlibarary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import static com.sadam.sadamlibarary.Tools.isLightColor;

public abstract class MyActivity extends AppCompatActivity {
    private static final String TAG = MyActivity.class.getSimpleName();

    public static void logE(Object object, String warning) {
        Log.e(object.getClass().getSimpleName(), warning);
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


        public int copyAndImmigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, String[] blackList, int r_raw_db_sqlite_id) {
            int count = -1;
            if (copyDatabaseFileToTheDirectory(r_raw_db_sqlite_id)) {
                count = immigrateDataToLitePal(resourse_databasename, resourse_tablename, resourse_database_version, target_modelClass, blackList);
                return count;
            } else {
                return count;
            }
        }

        /**
         * 把普通的sqlite数据库转移到LitePal数据库的工具
         *
         * @param resourse_databasename     普通数据库的“.db”文件名
         * @param resourse_tablename        普通数据库的 table 名
         * @param resourse_database_version 普通数据库的版本号
         * @param target_modelClass         LitePal数据库的映射模型类
         * @param blackList                 LitePal数据库映射模型里一些非字段变量 以及 与普通Sqlite数据库里不对口的数据
         * @return 返回成功转移的数据数量
         */
        protected int immigrateDataToLitePal(String resourse_databasename, String resourse_tablename, int resourse_database_version, Class target_modelClass, String[] blackList) {
            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MyActivity.this, resourse_databasename, null, resourse_database_version);
            SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.query(resourse_tablename, null, null, null, null, null, null, null);
            int lastcount = DataSupport.count(target_modelClass);
            Log.e(TAG, "原有的数据量：" + lastcount);
            if (lastcount == DataSupport.deleteAll(target_modelClass)) {
                Log.e(TAG, "初始化LitePal镜像数据库完成");
            } else {
                Log.e(TAG, "初始化LitePal镜像数据库失败");
            }
            Log.e(TAG, "开始迁移数据...........");
            Field[] fields = target_modelClass.getDeclaredFields();
            LinkedList<Field> linkedList = new LinkedList<>(Arrays.asList(fields));
            LinkedList<Field> willbedelet = new LinkedList<>();
            Log.e(TAG, fields.length + "  " + linkedList.size());
            for (Field field : linkedList) {
                for (String name : blackList) {
                    Log.e(TAG, field.getName() + "   " + name);
                    if (name.equals(field.getName())) {
                        willbedelet.add(field);
                        Log.e(TAG, "removed");
                    }
                }
            }
            Log.e(TAG, fields.length + "  " + linkedList.size());
            for (Field field : willbedelet) {
                linkedList.remove(field);
            }
            Log.e(TAG, fields.length + "  " + linkedList.size());

            if (cursor.moveToFirst()) {
                Log.e(TAG, "MoveToFirst");
                Log.e(TAG, "FirstWord");
                do {
                    try {
                        DataSupport object = (DataSupport) target_modelClass.newInstance();
                        for (Field field : linkedList) {
                            if (field.getName() == "id") continue;
                            else {
                                String methodName = "set" + field.getName();
                                Method method = target_modelClass.getDeclaredMethod(methodName, field.getType());
                                Class<?> type = field.getType();
                                int columnIndex = cursor.getColumnIndex(field.getName());
                                Object data = null;
                                if (type == String.class) {
                                    data = cursor.getString(columnIndex);
                                } else if (type == int.class) {
                                    data = cursor.getInt(columnIndex);
                                } else {
                                    data = cursor.getBlob(columnIndex);
                                }
//                                Log.e(TAG,data.toString());
                                method.invoke(object, data);
                            }
                        }
                        object.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            int count = DataSupport.count(target_modelClass);
            Log.e(TAG, "总共迁移数据量：" + count);
            return count;
        }

//        test5 taskmanger;
//        dfsfjkdsjl
//        mfsdm,fm.ds/
//        git test seven


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
            logE(this,dbfile);
            String dbPath = dbfile.substring(0,dbfile.length()-DB_NAME.length());
            logE(this,dbPath);
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

        public boolean isExistDataFile(String db_filename){
            return new File(getDBFilePath(db_filename)).exists();
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
