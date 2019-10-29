package MySQL;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 用这个工具之前，必须把 mysql-connector-java-5.1.36.jar  包添加到  项目的lib目录里面  ( 复制粘贴就ok ）
 * 下载地址：http://central.maven.org/maven2/mysql/mysql-connector-java/
 */
public class MySQLService {
    private String  host;
    private int port;
    private String database_name;
    private String user ;
    private String password ;
    private String url ;
    PreparedStatement statement;
    Connection connection = null;

    public MySQLService() {
        this("bj-cdb-ga7q38hy.sql.tencentcdb.com",61927,"app","root","1a2b3c4d5@s");
    }

    public MySQLService(String host, int port, String database_name, String user, String password) {
        this.host = host;
        this.port = port;
        this.database_name = database_name;
        this.user = user;
        this.password = password;
        this.url = "jdbc:mysql://"+host+":"+port+"/"+database_name+"?useSSL=false&serverTimezone=UTC";
    }

    /**  只负责连接
     * @param url
     * @param user
     * @param password
     * @return
     */
    public Connection getConnection(final String url,final String user,final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(url,user,password);
                    if(connection != null){
                        Log.e("mysql.getConnection","连接成功");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return connection;
    }


    private void  getConn(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url,user,password);
            if( connection != null){
                Log.e("mysql.getConn","连接成功");
            }
        } catch (ClassNotFoundException e) {
            Log.e("mysql.getconn..begin","没有找到com.mysql.jdbc.Driver");
            e.printStackTrace();
            Log.e("mysql.getconn..end","没有找到com.mysql.jdbc.Driver");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param mysql_cmd   MySQL的命令语句，如：“SELECT * FROM  users"
     * @return    类似于链表的形式返回所有查询结果，  resulSet.next()把  cursor 移到下一行   ，  然后  用  resultSet.getString( column_name ) 来获取具体的值
     */
    public ResultSet getData(String mysql_cmd){
        ResultSet resultSet = null;
        if(connection == null){
            getConn();
            try {
                statement = connection.prepareStatement(mysql_cmd);
                Log.e("mysql.getData","获取statement成功");
                resultSet = statement.executeQuery();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultSet;
    }

    /** 向数据库添加数据
     * @return
     */
    public int Insert(String table_name){
        int count = 0;
        String cmd = "INSERT INTO "+table_name + "(id,username,password,tel,email,registeredTime,sex,role_id,ip_id) VALUES(?,?,?,?,?,?,?,?,?)";
        if(connection==null){
            this.getConn();
            try {
                statement = connection.prepareStatement(cmd);
                statement.setInt(1,5);
                statement.setString(2,"@Sadam");
                statement.setString(3,"1sdfewrdfdsf");
                statement.setString(4,"18810720138");
                statement.setString(5,"1903249375@qq.com");
                statement.setTimestamp(6, new Timestamp(new Date().getTime()));
                statement.setString(7,"male");
                statement.setInt(8,1);
                statement.setInt(9,11);
                count = statement.executeUpdate();
                Log.e("mysql.insert","影响数据库的条数"+count);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }
}
