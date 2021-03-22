package com.mooc.ppjoke.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.model.BottomBar;
import com.mooc.ppjoke.model.Destination;
import com.mooc.ppjoke.utils.AppConfig;

import java.util.List;

public class AppBottomBar extends BottomNavigationView {
    private static int[] sIcons = new int[]{R.drawable.icon_tab_home, R.drawable.icon_tab_sofa, R.drawable.icon_tab_publish, R.drawable.icon_tab_find, R.drawable.icon_tab_mine};
    public AppBottomBar(@NonNull Context context) {
        this(context,null);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        BottomBar bottomBar= AppConfig.getsBottomBar();
        List<BottomBar.Tab> tabs=bottomBar.tabs;

        //二维数组，定义底部按钮被选中以及未被选中的状态
        //一维数组定义颜色
        int[][] states=new int[2][];
        states[0] =new int[]{android.R.attr.state_selected};
        states[1]=new int[]{};

        int[] colors=new int[]{Color.parseColor(bottomBar.activeColor),Color.parseColor(bottomBar.inActiveColor)};
        ColorStateList colorStateList=new ColorStateList(states,colors);

        //设置按钮
        setItemTextColor(colorStateList);
        setItemIconTintList(colorStateList);
        //设置按钮文本一直显示
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        //LABEL_VISIBILITY_LABELED:设置按钮的文本为一直显示模式
        //LABEL_VISIBILITY_AUTO:当按钮个数小于三个时一直显示，或者当按钮个数大于3个且小于5个时，被选中的那个按钮文本才会显示
        //LABEL_VISIBILITY_SELECTED：只有被选中的那个按钮的文本才会显示
        //LABEL_VISIBILITY_UNLABELED:所有的按钮文本都不显示

        setSelectedItemId(bottomBar.selectTab);

        for (int i = 0; i <tabs.size() ; i++) {
            BottomBar.Tab tab=tabs.get(i);
            //不显示在底部导航栏
            if (!tab.enable){
                return;
            }
            //tab的pageUrl和Destination的id是一一对应的，通过menu的id进行跳转
            int itemId=getId(tab.pageUrl);
            MenuItem item=getMenu().add(0,itemId,tab.index,tab.title);

            //给每个按钮设置icon
            item.setIcon(sIcons[tab.index]);

        }
        //设置按钮的大小，添加到导航栏之后才能设置按钮的大小
        //根据源码->先会remove之后，在通过for循环添加
        for (int i = 0; i <tabs.size(); i++) {
            BottomBar.Tab tab=tabs.get(i);
            //定义尺寸
            int iconSize=dp2px(tab.size);

            //根据跟进的源码
            BottomNavigationMenuView menuView= (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.index);
            itemView.setIconSize(iconSize);

            //给中间的大按钮进行着色
            if (TextUtils.isEmpty(tab.title)){
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                //禁止掉点按时 上下浮动的效果
                itemView.setShifting(false);

                /**
                 * 如果想要禁止掉所有按钮的点击浮动效果。
                 * 那么还需要给选中和未选中的按钮配置一样大小的字号。
                 *
                 *  在MainActivity布局的AppBottomBar标签增加如下配置，
                 *  @style/active，@style/inActive 在style.xml中
                 *  app:itemTextAppearanceActive="@style/active"
                 *  app:itemTextAppearanceInactive="@style/inActive"
                 */
            }

        }




    }

    private int dp2px(int dpValue) {
        DisplayMetrics metrics=getContext().getResources().getDisplayMetrics();
        return (int) (metrics.density*dpValue+0.5f);
    }

    //tab的pageUrl和Destination的id是一一对应的，通过menu的id进行跳转
    private int getId(String pageUrl) {
        Destination destination = AppConfig.getDestConfig().get(pageUrl);
        if (destination==null){
            return -1;
        }
        return destination.id;
    }
}
