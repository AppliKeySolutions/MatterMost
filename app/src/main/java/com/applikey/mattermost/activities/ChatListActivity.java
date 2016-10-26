package com.applikey.mattermost.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.applikey.mattermost.R;
import com.applikey.mattermost.adapters.ChatListPagerAdapter;
import com.applikey.mattermost.events.TabIndicatorRequested;
import com.applikey.mattermost.mvp.presenters.ChatListScreenPresenter;
import com.applikey.mattermost.mvp.views.ChatListScreenView;
import com.applikey.mattermost.views.TabBehavior;
import com.arellomobile.mvp.presenter.InjectPresenter;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChatListActivity extends BaseMvpActivity implements ChatListScreenView {

    @InjectPresenter
    ChatListScreenPresenter mPresenter;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.vpChatList)
    ViewPager mViewPager;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ButterKnife.bind(this);

        initView();

        mEventBus.register(this);
    }

    private void initView() {
        mPresenter.applyInitialViewState();

        mViewPager.setAdapter(new ChatListPagerAdapter(getSupportFragmentManager()));

        mTabLayout.setupWithViewPager(mViewPager);
        final int tabCount = mTabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            final TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.tab_chat_list);
                tab.setIcon(TabBehavior.getItemBehavior(i).getIcon());

                final View customTab = tab.getCustomView();
                if (customTab != null) {
                    final View notificationIcon = customTab.findViewById(R.id.iv_notification_icon);
                    mTabIndicatorModel.register(TabBehavior.values()[i + 1],
                            (ImageView) notificationIcon);
                }
            }
        }

        final TabSelectedListener mOnTabSelectedListener = new TabSelectedListener(mViewPager);
        mTabLayout.addOnTabSelectedListener(mOnTabSelectedListener);
        mOnTabSelectedListener.onTabReselected(mTabLayout.getTabAt(0));
        mViewPager.setOffscreenPageLimit(tabCount - 1);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));

        mNavigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.start_new_chat:
                    return true; //TODO replace with start new chat logic
                case R.id.create_channel:
                    return true; //TODO replace with create channel logic
                case R.id.settings:
                    return true; //TODO replace with settings logic
                case R.id.logout:
                    mPresenter.logout();
                    return true;
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });
    }

    @Override
    public void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void logout() {
        final Intent intent = new Intent(this, ChooseServerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.unSubscribe();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void on(TabIndicatorRequested event) {
        mTabIndicatorModel.handleEvent(event);
    }

    public static Intent getIntent(Context context) {
        final Intent intent = new Intent(context, ChatListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private class TabSelectedListener extends TabLayout.ViewPagerOnTabSelectedListener {

        private int selectedTabColor = -1;
        private int unSelectedTabColor = -1;

        TabSelectedListener(ViewPager viewPager) {
            super(viewPager);
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            super.onTabSelected(tab);
            final Drawable icon = tab.getIcon();
            if (icon != null) {
                icon.setColorFilter(getSelectedTabColor(), PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            super.onTabUnselected(tab);
            final Drawable icon = tab.getIcon();
            if (icon != null) {
                icon.setColorFilter(getUnSelectedTabColor(), PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            super.onTabReselected(tab);
            final Drawable icon = tab.getIcon();
            if (icon != null) {
                icon.setColorFilter(getSelectedTabColor(), PorterDuff.Mode.SRC_IN);
            }
        }

        private int getSelectedTabColor() {
            if (selectedTabColor == -1) {
                selectedTabColor = ContextCompat.getColor(ChatListActivity.this,
                        R.color.tabSelected);
            }
            return selectedTabColor;
        }

        private int getUnSelectedTabColor() {
            if (unSelectedTabColor == -1) {
                unSelectedTabColor = ContextCompat.getColor(ChatListActivity.this,
                        R.color.tabUnSelected);
            }
            return unSelectedTabColor;
        }
    }

    private final TabIndicatorModel mTabIndicatorModel = new TabIndicatorModel();

    private class TabIndicatorModel {

        private final Object mutex = new Object();

        private final Map<TabBehavior, Boolean> mIndicatorVisibilities = new HashMap<>();
        private final Map<TabBehavior, ImageView> mIndicators = new HashMap<>();

        void handleEvent(TabIndicatorRequested event) {
            synchronized (mutex) {
                final TabBehavior tab = event.getBehavior();
                mIndicatorVisibilities.put(tab, event.isVisible());
                updateVisibility(tab, event.isVisible());
            }
        }

        void register(TabBehavior tab, ImageView indicator) {
            synchronized (mutex) {
                mIndicators.put(tab, indicator);
                final boolean visible = mIndicatorVisibilities.containsKey(tab)
                        ? mIndicatorVisibilities.get(tab) : false;
                updateVisibility(tab, visible);
            }
        }

        private void updateVisibility(TabBehavior tab, boolean isVisible) {
            if (mIndicators.containsKey(tab)) {
                mIndicators.get(tab).setVisibility(isVisible ? VISIBLE : GONE);
            }
        }
    }
}
