package com.mooc.ppjoke;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mooc.libnetwork.ApiResponse;
import com.mooc.libnetwork.GetRequest;
import com.mooc.libnetwork.JsonCallback;
import com.mooc.ppjoke.model.Destination;
import com.mooc.ppjoke.model.User;
import com.mooc.ppjoke.ui.login.UserManager;
import com.mooc.ppjoke.utils.AppConfig;
import com.mooc.ppjoke.utils.NavGraphBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Fragment fragment=getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);


        //不需要资源文件中NavGraph的资源引用了
        NavGraphBuilder.build(navController,this,fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);

        //测试代码->测试泛型擦除
        GetRequest<JSONObject> request = new GetRequest<>("www.mooc.com");
        request.execute();

        request.execute(new JsonCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                super.onSuccess(response);
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String,Destination>> iterator=destConfig.entrySet().iterator();
        //遍历 target destination 是否需要登录拦截
        while (iterator.hasNext()){
            Map.Entry<String,Destination> entry=iterator.next();
            Destination value=entry.getValue();
            //如果登录成功
            if (value!=null&&!UserManager.get().isLogin()&&value.needLogin&&value.id==menuItem.getItemId()){
                //如果登录成功
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user!=null){
                            navView.setSelectedItemId(menuItem.getItemId());
                        }
                    }
                });
                return false;
            }
        }

        //通过NavController来跳转到相关的页面
        navController.navigate(menuItem.getItemId());
        //返回false,表示未被着色
        return !TextUtils.isEmpty(menuItem.getTitle());
    }
}