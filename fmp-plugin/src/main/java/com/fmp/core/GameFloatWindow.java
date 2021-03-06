package com.fmp.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fmp.view.SeekBarView;
import com.fmp.view.SwitchView;
import com.mojang.minecraftpe.MainActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.WINDOW_SERVICE;

@SuppressLint({"StaticFieldLeak", "ClickableViewAccessibility"})
public class GameFloatWindow extends GameManager {
    private static GameFloatWindow gameFloatWindow;
    private final String TAG_floatWindow = "floatWindow";
    private final String TAG_floatKillingView = "floatKillingView";
    private final String TAG_floatClickKillView = "floatClickKillView";
    private final String TAG_floatMenu = "floatMenu";
    private final String TAG_floatFunction = "floatFunction";
    private final String TAG_floatPlugin = "floatPlugin";
    private final String TAG_floatBrowser = "floatBrowser";
    private Activity activity;

    private WindowManager windowManager;

    private WindowManager.LayoutParams mainMenuParams = new WindowManager.LayoutParams();
    private WindowManager.LayoutParams mainFunctionParams = new WindowManager.LayoutParams();
    private WindowManager.LayoutParams mainPluginParams = new WindowManager.LayoutParams();
    private List<WindowManager.LayoutParams> mainBrowserParamsList = new ArrayList<>();

    private Drawable switchOnDrawable;
    private Drawable switchOffDrawable;

    private ImageView floatWindowView;
    private LinearLayout floatKillingView;
    private LinearLayout floatClickKillView;
    private View mainMenuLayout;
    private View mainFunctionLayout;
    private View mainPluginLayout;
    private List<View> mainBrowserLayoutList = new ArrayList<>();
    //private View mainBrowserLayout;

    private TextView gamePluginNoItem;
    private GamePluginListAdapter gamePluginListAdapter;

    private long dialogAlphaTipTime = System.currentTimeMillis();

    public static GameFloatWindow getInstance() {
        if (gameFloatWindow == null) {
            gameFloatWindow = new GameFloatWindow();
        }
        return gameFloatWindow;
    }

    /*
     * ???????????????
     */
    public void init(Activity activity) {
        if (this.activity == null) {
            this.activity = activity;
        } else {
            return;
        }
        // ??????WindowManager??????
        this.windowManager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
        try {
            this.switchOnDrawable = new HelperResources("ic_lock_outline_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable();
            this.switchOffDrawable = new HelperResources("ic_lock_open_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable();
            showFloatWindow();
        } catch (NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

    }

    private Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(null, newbmp);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /*
     * ????????????
     */
    private void showFloatWindow() {
        if (windowManager != null) {
            try {
                int floatWindowX = (int) get("floatWindowX", 300);
                int floatWindowY = (int) get("floatWindowY", 300);
                // ??????LayoutParam
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                layoutParams.format = PixelFormat.RGBA_8888;
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                layoutParams.windowAnimations = android.R.style.Animation_Translucent;
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.alpha = 0.7f;
                layoutParams.x = floatWindowX;
                layoutParams.y = floatWindowY;
                // ?????????????????????
                floatWindowView = new ImageView(activity);
                floatWindowView.setTag(TAG_floatWindow);
                floatWindowView.setBackgroundColor(Color.TRANSPARENT);
                int[] display = getDisplay();
                floatWindowView.setImageDrawable(zoomDrawable(new HelperResources("emp_logo", HelperResources.RESOURCES_DRAWABLE).getDrawable(), display[0], display[1]));
                floatWindowView.setOnTouchListener(new FloatingOnTouchListener(layoutParams));
                floatWindowView.setOnClickListener(new FloatWindowClickListener());
                // ???????????????????????????WindowManager
                windowManager.addView(floatWindowView, layoutParams);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * ?????????
     */
    private void showFloatMenu() {
        if (windowManager != null) {
            toast("??????????????????????????????????????????????????????????????????~");
            try {
                int floatMenuX = (int) get("floatMenuX", 300);
                int floatMenuY = (int) get("floatMenuY", 300);
                int dialog_alpha = (Integer) get("dialog_alpha", 100);
                float alpha = ((float) dialog_alpha) / 100;
                // ??????LayoutParam
                mainMenuParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                mainMenuParams.format = PixelFormat.RGBA_8888;
                mainMenuParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mainMenuParams.windowAnimations = android.R.style.Animation_Translucent;
                mainMenuParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mainMenuParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mainMenuParams.alpha = alpha;
                mainMenuParams.x = floatMenuX;
                mainMenuParams.y = floatMenuY;
                // ?????????????????????
                mainMenuLayout = new HelperResources("fmp_float_dialog_menu", HelperResources.RESOURCES_LAYOUT).getLayout(activity);
                mainMenuLayout.setTag(TAG_floatMenu);
                FloatingOnTouchListener listener = new FloatingOnTouchListener(mainMenuParams);
                mainMenuLayout.setOnTouchListener(listener);

                ImageView dialogLock = mainMenuLayout.findViewWithTag("dialog_lock");
                ImageView dialogDismiss = mainMenuLayout.findViewWithTag("dialog_exit");
                SwitchView removeCheck = mainMenuLayout.findViewWithTag("remove_check");
                SeekBarView dialogAlpha = mainMenuLayout.findViewWithTag("dialog_alpha");

                Button function = mainMenuLayout.findViewWithTag("function");
                Button plugin = mainMenuLayout.findViewWithTag("plugin");
                Button browser = mainMenuLayout.findViewWithTag("browser");
                Button logout = mainMenuLayout.findViewWithTag("logout");

                listener.setLockView(dialogLock);
                dialogDismiss.setImageDrawable(new HelperResources("ic_highlight_off_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable());
                dialogAlpha.setMax(100);
                dialogAlpha.setProgress((Integer) get(String.valueOf(dialogAlpha.getTag()), dialogAlpha.getMax()));

                FloatMenuClickListener clickListener = new FloatMenuClickListener();
                dialogDismiss.setOnClickListener(clickListener);
                removeCheck.setOnCheckedChangeListener(clickListener);
                dialogAlpha.setOnSeekBarChangeListener(clickListener);
                function.setOnClickListener(clickListener);
                plugin.setOnClickListener(clickListener);
                browser.setOnClickListener(clickListener);
                logout.setOnClickListener(clickListener);
                // ???????????????????????????WindowManager
                windowManager.addView(mainMenuLayout, mainMenuParams);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFunction() {
        if (windowManager != null) {
            try {
                int x = (int) get("floatFunctionX", 300);
                int y = (int) get("floatFunctionY", 300);
                int dialog_alpha = (Integer) get("dialog_alpha", 100);
                float alpha = ((float) dialog_alpha) / 100;
                // ??????LayoutParam
                mainFunctionParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                mainFunctionParams.format = PixelFormat.RGBA_8888;
                mainFunctionParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mainFunctionParams.windowAnimations = android.R.style.Animation_Translucent;
                mainFunctionParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mainFunctionParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mainFunctionParams.alpha = alpha;
                mainFunctionParams.x = x;
                mainFunctionParams.y = y;
                // ?????????????????????
                mainFunctionLayout = new HelperResources("fmp_float_dialog_function", HelperResources.RESOURCES_LAYOUT).getLayout(activity);
                mainFunctionLayout.setTag(TAG_floatFunction);
                FloatingOnTouchListener listener = new FloatingOnTouchListener(mainFunctionParams);
                mainFunctionLayout.setOnTouchListener(listener);

                ImageView dialogLock = mainFunctionLayout.findViewWithTag("dialog_lock");
                ImageView dialogDismiss = mainFunctionLayout.findViewWithTag("dialog_exit");

                TextView killingMsg = mainFunctionLayout.findViewWithTag("fun_killing_msg");
                TextView clickKillMsg=mainFunctionLayout.findViewWithTag("fun_click_kill_msg");
                SwitchView addItemToOffhand = mainFunctionLayout.findViewWithTag("fun_item_to_offhand");
                SwitchView carriedItemToOffhand = mainFunctionLayout.findViewWithTag("fun_carried_to_offhand");
                SwitchView allItemAllowOffhand = mainFunctionLayout.findViewWithTag("fun_item_allow_offhand");
                SwitchView killingMode = mainFunctionLayout.findViewWithTag("fun_killing");
                SwitchView clickKillMode = mainFunctionLayout.findViewWithTag("fun_click_kill");
                SwitchView setOperator = mainFunctionLayout.findViewWithTag("fun_operator");
                SwitchView destroyModeTen = mainFunctionLayout.findViewWithTag("fun_destroy_ten");
                SwitchView destroyModeClick = mainFunctionLayout.findViewWithTag("fun_destroy_click");
                SwitchView fastBuild = mainFunctionLayout.findViewWithTag("fun_fast_build");

                List<View> dividerList=new ArrayList<>();
                dividerList.add(mainFunctionLayout.findViewWithTag("divider1"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider2"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider3"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider4"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider5"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider6"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider7"));
                dividerList.add(mainFunctionLayout.findViewWithTag("divider8"));

                LinearLayout layoutKilling = mainFunctionLayout.findViewWithTag("layout_killing");
                LinearLayout layoutClickKill = mainFunctionLayout.findViewWithTag("layout_click_kill");
                LinearLayout layoutOperator = mainFunctionLayout.findViewWithTag("layout_operator");
                LinearLayout layoutDestroyModeTen = mainFunctionLayout.findViewWithTag("layout_destroy_ten");
                LinearLayout layoutDestroyClick = mainFunctionLayout.findViewWithTag("layout_destroy_click");
                LinearLayout layoutFastBuild = mainFunctionLayout.findViewWithTag("layout_fast_build");
                /*try {
                    if (getLevel() <= 1) {
                        dividerList.get(0).setVisibility(View.GONE);
                        dividerList.get(1).setVisibility(View.GONE);
                        dividerList.get(5).setVisibility(View.GONE);
                        dividerList.get(6).setVisibility(View.GONE);
                        dividerList.get(7).setVisibility(View.GONE);
                        layoutKilling.setVisibility(View.GONE);
                        layoutClickKill.setVisibility(View.GONE);
                        layoutDestroyModeTen.setVisibility(View.GONE);
                        layoutDestroyClick.setVisibility(View.GONE);
                        layoutFastBuild.setVisibility(View.GONE);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }*/

                listener.setLockView(dialogLock);
                dialogDismiss.setImageDrawable(new HelperResources("ic_highlight_off_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable());
                addItemToOffhand.setChecked(true);//????????????

                FunctionClickListener clickListener = new FunctionClickListener();
                dialogDismiss.setOnClickListener(clickListener);
                addItemToOffhand.setOnCheckedChangeListener(clickListener);
                carriedItemToOffhand.setOnCheckedChangeListener(clickListener);
                allItemAllowOffhand.setOnCheckedChangeListener(clickListener);
                killingMsg.setOnLongClickListener(clickListener);
                killingMode.setOnCheckedChangeListener(clickListener);
                clickKillMsg.setOnLongClickListener(clickListener);
                clickKillMode.setOnCheckedChangeListener(clickListener);
                setOperator.setOnCheckedChangeListener(clickListener);
                destroyModeTen.setOnCheckedChangeListener(clickListener);
                destroyModeClick.setOnCheckedChangeListener(clickListener);
                fastBuild.setOnCheckedChangeListener(clickListener);
                // ???????????????????????????WindowManager
                windowManager.addView(mainFunctionLayout, mainFunctionParams);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * ????????????
     */
    private void showPluginList() {
        if (windowManager != null) {
            try {
                int pluginListX = (int) get("pluginListX", 300);
                int pluginListY = (int) get("pluginListY", 300);
                int dialog_alpha = (Integer) get("dialog_alpha", 100);
                float alpha = ((float) dialog_alpha) / 100;
                // ??????LayoutParam
                mainPluginParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                mainPluginParams.format = PixelFormat.RGBA_8888;
                mainPluginParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mainPluginParams.windowAnimations = android.R.style.Animation_Translucent;
                mainPluginParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mainPluginParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mainPluginParams.alpha = alpha;
                mainPluginParams.x = pluginListX;
                mainPluginParams.y = pluginListY;
                // ?????????????????????
                mainPluginLayout = new HelperResources("fmp_float_dialog_plugin", HelperResources.RESOURCES_LAYOUT).getLayout(activity);
                mainPluginLayout.setTag(TAG_floatPlugin);
                FloatingOnTouchListener listener = new FloatingOnTouchListener(mainPluginParams);
                mainPluginLayout.setOnTouchListener(listener);

                ImageView dialogLock = mainPluginLayout.findViewWithTag("dialog_lock");
                ImageView dialogDismiss = mainPluginLayout.findViewWithTag("dialog_exit");
                gamePluginNoItem = mainPluginLayout.findViewWithTag("plugin_no_item");
                ListView pluginList = mainPluginLayout.findViewWithTag("plugin_list");

                listener.setLockView(dialogLock);
                dialogDismiss.setImageDrawable(new HelperResources("ic_highlight_off_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable());
                dialogDismiss.setOnClickListener(new PluginClickListener());
                List<PluginItem> items = GameManager.getInstance().getPluginItems();
                if (items.size() == 0) {
                    gamePluginNoItem.setVisibility(View.VISIBLE);
                    toast("????????????????????????????????????????????????????????????");
                }
                gamePluginListAdapter = new GamePluginListAdapter(activity, items);
                pluginList.setAdapter(gamePluginListAdapter);

                // ???????????????????????????WindowManager
                windowManager.addView(mainPluginLayout, mainPluginParams);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void showFloatBrowser() {
        if (windowManager != null) {
            try {
                int floatBrowserX = (int) get("floatBrowserX", 300);
                int floatBrowserY = (int) get("floatBrowserY", 300);
                int dialog_alpha = (Integer) get("dialog_alpha", 100);
                float alpha = ((float) dialog_alpha) / 100;
                // ??????LayoutParam
                WindowManager.LayoutParams mainBrowserParams = new WindowManager.LayoutParams();
                mainBrowserParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                mainBrowserParams.format = PixelFormat.RGBA_8888;
                mainBrowserParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                mainBrowserParams.windowAnimations = android.R.style.Animation_Translucent;
                mainBrowserParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mainBrowserParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mainBrowserParams.alpha = alpha;
                mainBrowserParams.x = floatBrowserX;
                mainBrowserParams.y = floatBrowserY;
                mainBrowserParamsList.add(mainBrowserParams);
                // ?????????????????????
                View layout = new HelperResources("fmp_float_dialog_browser", HelperResources.RESOURCES_LAYOUT).getLayout(activity);
                mainBrowserLayoutList.add(layout);
                layout.setTag(TAG_floatBrowser);
                FloatingOnTouchListener listener = new FloatingOnTouchListener(mainBrowserParams);
                layout.setOnTouchListener(listener);

                ImageView dialogLock = layout.findViewWithTag("dialog_lock");
                ImageView dialogDismiss = layout.findViewWithTag("dialog_exit");

                listener.setLockView(dialogLock);
                dialogDismiss.setImageDrawable(new HelperResources("ic_highlight_off_black_24dp", HelperResources.RESOURCES_DRAWABLE).getDrawable());

                FloatBrowserClickListener clickListener = new FloatBrowserClickListener();
                clickListener.setLayoutParams(mainBrowserParams);

                WebView webView = layout.findViewWithTag("browser");
                clickListener.setWebView(webView);
                WebSettings settings = webView.getSettings();
                //settings.setJavaScriptEnabled(true);
                //settings.setJavaScriptCanOpenWindowsAutomatically(true);
                //settings.setUseWideViewPort(true);
                //settings.setJavaScriptEnabled(true);
                // ???JavaScript??????????????????windows
                //settings.setJavaScriptCanOpenWindowsAutomatically(true);
                // ????????????
                //settings.setAppCacheEnabled(true);
                // ??????????????????,?????????????????????
                //settings.setCacheMode(WebSettings.FORCE_DARK_AUTO);

                // ??????????????????
                //webSettings.setAppCachePath("");
                // ????????????(?????????????????????)
                //settings.setSupportZoom(true);
                // ?????????????????????????????????
                //settings.setUseWideViewPort(true);
                // ????????????????????????,?????????????????????
                // ????????????NARROW_COLUMNS
                //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                // ????????????????????????????????????
                //settings.setDisplayZoomControls(true);
                // ????????????????????????
                settings.setDefaultFontSize(12);

                // ??????JS
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setBuiltInZoomControls(true);
                settings.setDisplayZoomControls(true);
                settings.setLoadWithOverviewMode(true);
                // ????????????
                settings.setPluginState(WebSettings.PluginState.ON);
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
                // ???????????????
                settings.setUseWideViewPort(true);
                settings.setLoadWithOverviewMode(true);
                // ????????????
                settings.setSupportZoom(false);//????????????????????????????????????
                // ????????????????????????
                settings.setDisplayZoomControls(false);
                // ????????????????????????
                settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                settings.supportMultipleWindows();
                settings.setSupportMultipleWindows(true);
                // ??????????????????
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                settings.setAppCacheEnabled(true);
                settings.setAppCachePath(webView.getContext().getCacheDir().getAbsolutePath());
                // ?????????????????????
                settings.setAllowFileAccess(true);
                settings.setNeedInitialFocus(true);
                // ????????????????????????
                settings.setLoadsImagesAutomatically(true);
                settings.setNeedInitialFocus(true);
                // ??????????????????
                settings.setDefaultTextEncodingName("UTF-8");

                //webView.setWebViewClient(clickListener);
                // ????????????????????????
                webView.requestFocusFromTouch();
                webView.setHorizontalFadingEdgeEnabled(true);
                webView.setVerticalFadingEdgeEnabled(false);
                webView.setVerticalScrollBarEnabled(false);
                webView.setWebViewClient(new android.webkit.WebViewClient());
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                webView.setWebChromeClient(new WebChromeClient());

                clickListener.getMap().put("User-Agent", "Android");
                webView.loadUrl("https://www.baidu.com", clickListener.getMap());
                layout.findViewWithTag("Goon").setOnClickListener(clickListener);
                //????????????
                layout.findViewWithTag("refresh").setOnClickListener(clickListener);
                //????????????
                layout.findViewWithTag("next").setOnClickListener(clickListener);
                //????????????
                layout.findViewWithTag("back").setOnClickListener(clickListener);
                //??????????????????
                layout.findViewWithTag("switch").setOnClickListener(clickListener);

                clickListener.setLayout(layout);
                dialogDismiss.setOnClickListener(clickListener);

                // ???????????????????????????WindowManager
                windowManager.addView(layout, mainBrowserParams);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | PackageManager.NameNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private int[] getDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            //??????
            return new int[]{dm.heightPixels / 4, dm.heightPixels / 4};
        } else {
            //??????
            return new int[]{dm.widthPixels / 4, dm.widthPixels / 4};
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void put(String key, Object object) {
        if (object == null) {
            return;
        }
        SharedPreferences sp = activity.getSharedPreferences("HelperData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public Object get(String key, Object defaultObject) {
        SharedPreferences sp = activity.getSharedPreferences("HelperData", Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        } else {
            return null;
        }
    }

    private void toast(String str) {
        Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
    }

    private class FloatBrowserClickListener implements View.OnClickListener {
        private WindowManager.LayoutParams layoutParams;
        private View layout;
        private WebView webView;
        private boolean show;
        private Map<String, String> map = new java.util.HashMap<>();

        void setLayoutParams(WindowManager.LayoutParams params) {
            this.layoutParams = params;
        }

        public void setLayout(View view) {
            this.layout = view;
        }

        void setWebView(WebView view) {
            this.webView = view;
        }

        public Map<String, String> getMap() {
            return map;
        }

        @Override
        public void onClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case "dialog_exit":
                    if (layout != null) {
                        windowManager.removeView(layout);
                        mainBrowserLayoutList.remove(layout);
                        mainBrowserParamsList.remove(layoutParams);
                    }
                    break;
                case "Goon":
                    android.widget.EditText e = layout.findViewWithTag("edit");
                    String es = e.getText().toString().intern();
                    if (isZH_CN(es)) {
                        if (isURL(es)) {
                            webView.loadUrl(es, map);
                        } else {
                            webView.loadUrl("https://www.baidu.com/s?wd=" + es, map);
                        }
                    } else if (!isZH_CN(es)) {
                        if (isURL(es)) {
                            webView.loadUrl(es, map);
                        } else {
                            webView.loadUrl("https://www.baidu.com/s?wd=" + es, map);
                        }
                    }
                    break;
                case "refresh":
                    webView.reload();
                    break;
                case "next":
                    if ((webView.canGoForward())) {
                        webView.goForward();
                    } else {
                        toast("??????????????????");
                    }
                    break;
                case "back":
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        toast("??????????????????");
                    }
                    break;
                case "switch":
                    if (!show) {
                        show = true;
                        webView.loadUrl("view-source:" + webView.getUrl());
                    } else {
                        show = false;
                        webView.goBack();
                    }
                    break;
            }
        }

        //?????????????????????
        boolean isZH_CN(String str) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("[\u4e00-\u9fa5]");
            java.util.regex.Matcher m = p.matcher(str);
            return m.find();
        }

        //?????????????????????
        boolean isURL(String url) {
            String str = "^((http|https)://)([a-zA-Z0-9_\\-.])((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(]?)$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(str);
            java.util.regex.Matcher m = p.matcher(url);
            return m.matches();
        }
    }

    private class FloatWindowClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case TAG_floatWindow:
                    if (mainMenuLayout == null) {
                        showFloatMenu();
                        v.setVisibility(View.GONE);
                    } else {
                        if (mainMenuLayout.getVisibility() == View.GONE) {
                            mainMenuLayout.setVisibility(View.VISIBLE);
                            v.setVisibility(View.GONE);
                        } else {
                            mainMenuLayout.setVisibility(View.GONE);
                            v.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }

        }
    }

    private class PluginClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case "dialog_exit":
                    mainPluginLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private class FloatMenuClickListener implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, SwitchView.OnCheckedChangeListener {

        @Override
        public void onClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case "dialog_exit":
                    mainMenuLayout.setVisibility(View.GONE);
                    floatWindowView.setVisibility(View.VISIBLE);
                    break;
                case "function":
                    if (mainFunctionLayout == null) {
                        showFunction();
                    } else {
                        if (mainFunctionLayout.getVisibility() == View.GONE) {
                            mainFunctionLayout.setVisibility(View.VISIBLE);
                        } else {
                            mainFunctionLayout.setVisibility(View.GONE);
                        }
                    }
                    break;
                case "plugin":
                    if (mainPluginLayout == null) {
                        showPluginList();
                    } else {
                        if (mainPluginLayout.getVisibility() == View.GONE) {
                            mainPluginLayout.setVisibility(View.VISIBLE);
                            List<PluginItem> items = GameManager.getInstance().getPluginItems();
                            //??????????????????????????????
                            gamePluginNoItem.setVisibility(items.size() == 0 ? View.VISIBLE : View.GONE);
                            //???????????????????????????
                            gamePluginListAdapter.notifyDataSetChanged();
                        } else {
                            mainPluginLayout.setVisibility(View.GONE);
                        }
                    }
                    break;
                case "browser":
                    showFloatBrowser();
                    break;
                case "logout":
                    try {
                        GameManager.logout();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress >= 20) {
                mainMenuParams.alpha = ((float) progress) / 100;
                mainFunctionParams.alpha = ((float) progress) / 100;
                mainPluginParams.alpha = ((float) progress) / 100;
                if (mainMenuLayout != null)
                    windowManager.updateViewLayout(mainMenuLayout, mainMenuParams);
                if (mainFunctionLayout != null)
                    windowManager.updateViewLayout(mainFunctionLayout, mainFunctionParams);
                if (mainPluginLayout != null)
                    windowManager.updateViewLayout(mainPluginLayout, mainPluginParams);
                if (mainBrowserParamsList.size() > 0 && mainBrowserLayoutList.size() > 0) {
                    int i = 0;
                    for (WindowManager.LayoutParams params : mainBrowserParamsList) {
                        params.alpha = ((float) progress) / 100;
                        windowManager.updateViewLayout(mainBrowserLayoutList.get(i), params);
                        i++;
                    }
                }
            } else {
                seekBar.setProgress(20);
                long curTime = System.currentTimeMillis();
                if (curTime - dialogAlphaTipTime >= 3000) {
                    dialogAlphaTipTime = System.currentTimeMillis();
                    toast("????????????????????????????????????");
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            put(String.valueOf(seekBar.getTag()), seekBar.getProgress());
        }

        @Override
        public void onCheckedChanged(SwitchView switchView, boolean isChecked) {
            switch (String.valueOf(switchView.getTag())) {
                case "remove_check":
                    try {
                        MainActivity mainActivity = MainActivity.getInstance();
                        Class<?> clazz = mainActivity.getClass();
                        Field launchedFromDuowan = clazz.getDeclaredField("launchedFromDuowan");
                        launchedFromDuowan.setAccessible(true);
                        launchedFromDuowan.set(mainActivity, !isChecked);
                        Field canLaunchFromDuowan = clazz.getDeclaredField("canLaunchFromDuowan");
                        canLaunchFromDuowan.setAccessible(true);
                        canLaunchFromDuowan.set(mainActivity, !isChecked);
                        if (isChecked) {
                            toast("??????????????????????????????????????????????????????????????????");
                            Field duowanIntentData = clazz.getDeclaredField("duowanIntentData");
                            duowanIntentData.setAccessible(true);
                            duowanIntentData.set(mainActivity, "");
                        }
                    } catch (Throwable e) {
                        switchView.setChecked(!isChecked);
                        toast("????????????");
                    }
                    break;
            }
        }
    }

    private class FunctionClickListener implements View.OnClickListener, View.OnLongClickListener, SwitchView.OnCheckedChangeListener {

        @Override
        public void onClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case "dialog_exit":
                    mainFunctionLayout.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onCheckedChanged(SwitchView switchView, boolean isChecked) {
            try {
                switch (String.valueOf(switchView.getTag())) {
                    case "fun_item_to_offhand":
                        nativeAddItemToOffhand(isChecked);
                        break;
                    case "fun_carried_to_offhand":
                        nativeCarriedItemToOffhand(isChecked);
                        break;
                    case "fun_item_allow_offhand":
                        nativeAllItemAllowOffhand(isChecked);
                        break;
                    case "fun_killing":
                        nativeKillingMode(isChecked);
                        break;
                    case "fun_click_kill":
                        nativeClickKillMode(isChecked);
                        break;
                    case "fun_operator":
                        nativePlayerSetOperator("-1", isChecked);
                        break;
                    case "fun_destroy_ten":
                        nativeDestroyModeTen(isChecked);
                        break;
                    case "fun_destroy_click":
                        nativeDestroyModeClick(isChecked);
                        break;
                    case "fun_fast_build":
                        nativeFastBuild(isChecked);
                        break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (String.valueOf(v.getTag())) {
                case "fun_killing_msg":
                    if (windowManager != null) {
                        if (floatKillingView == null) {
                            int[] display = getDisplay();
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                            layoutParams.format = PixelFormat.RGBA_8888;
                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            layoutParams.windowAnimations = android.R.style.Animation_Translucent;
                            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            layoutParams.alpha = 0.7f;
                            layoutParams.x = display[0] / 2;
                            layoutParams.y = display[1] / 2;

                            floatKillingView = new LinearLayout(activity);
                            floatKillingView.setTag(TAG_floatKillingView);
                            floatKillingView.setOrientation(LinearLayout.HORIZONTAL);
                            TextView tip = new TextView(activity);
                            tip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            tip.setTextColor(Color.RED);
                            tip.setGravity(Gravity.CENTER);
                            tip.setText("????????????");
                            floatKillingView.addView(tip);
                            SwitchView killing = new SwitchView(activity);
                            killing.setTag("fun_killing");
                            killing.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                            killing.setChecked(((SwitchView) mainFunctionLayout.findViewWithTag("fun_killing")).isChecked());
                            killing.setOnCheckedChangeListener(this);
                            floatKillingView.addView(killing);

                            FloatingOnTouchListener listener = new FloatingOnTouchListener(layoutParams);
                            floatKillingView.setOnTouchListener(listener);

                            windowManager.addView(floatKillingView, layoutParams);
                        } else {
                            windowManager.removeView(floatKillingView);
                            floatKillingView = null;
                        }
                    }
                    break;
                case "fun_click_kill_msg":
                    if (windowManager != null) {
                        if (floatClickKillView == null) {
                            int[] display = getDisplay();
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                            layoutParams.format = PixelFormat.RGBA_8888;
                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            layoutParams.windowAnimations = android.R.style.Animation_Translucent;
                            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            layoutParams.alpha = 0.7f;
                            layoutParams.x = display[0] / 2;
                            layoutParams.y = display[1] / 2;

                            floatClickKillView = new LinearLayout(activity);
                            floatClickKillView.setTag(TAG_floatClickKillView);
                            floatClickKillView.setOrientation(LinearLayout.HORIZONTAL);
                            TextView tip = new TextView(activity);
                            tip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            tip.setTextColor(Color.RED);
                            tip.setGravity(Gravity.CENTER);
                            tip.setText("????????????");
                            floatClickKillView.addView(tip);
                            SwitchView killing = new SwitchView(activity);
                            killing.setTag("fun_click_kill");
                            killing.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                            killing.setChecked(((SwitchView) mainFunctionLayout.findViewWithTag("fun_click_kill")).isChecked());
                            killing.setOnCheckedChangeListener(this);
                            floatClickKillView.addView(killing);

                            FloatingOnTouchListener listener = new FloatingOnTouchListener(layoutParams);
                            floatClickKillView.setOnTouchListener(listener);

                            windowManager.addView(floatClickKillView, layoutParams);
                        } else {
                            windowManager.removeView(floatClickKillView);
                            floatClickKillView = null;
                        }
                    }
                    break;
            }
            return true;
        }
    }

    private class HelperResources {
        static final String RESOURCES_STRING = "string";
        static final String RESOURCES_DRAWABLE = "drawable";
        static final String RESOURCES_LAYOUT = "layout";
        private String packageName = "net.fmp.helper";
        private String name;
        private String type;
        private Resources resources;

        @SuppressWarnings("JavaReflectionMemberAccess")
        @SuppressLint("DiscouragedPrivateApi")
        HelperResources(String name, String resType) throws NoSuchMethodException, PackageManager.NameNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
            this.name = name;
            this.type = resType;
            AssetManager assetMAnager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(assetMAnager, activity.getPackageManager()
                    .getApplicationInfo(packageName, 0).sourceDir);
            resources = new Resources(assetMAnager, activity.getResources().getDisplayMetrics(), activity.getResources().getConfiguration());
        }

        public String getString() {
            return resources.getString(resources.getIdentifier(name, type, packageName));
        }

        public Drawable getDrawable() {
            return resources.getDrawable(resources.getIdentifier(name, type, packageName));
        }

        View getLayout(Context context) {
            return LayoutInflater.from(context).inflate(resources.getLayout(resources.getIdentifier(name, type, packageName)), null);
        }

        XmlResourceParser getLayoutXml() {
            return resources.getLayout(resources.getIdentifier(name, type, packageName));
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener, View.OnClickListener {
        private WindowManager.LayoutParams layoutParams;
        private ImageView lockView;
        private int x;
        private int y;

        FloatingOnTouchListener(WindowManager.LayoutParams params) {
            this.layoutParams = params;
        }

        void setLockView(ImageView view) {
            this.lockView = view;
            view.setImageDrawable(switchOffDrawable);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView)
                if (lockView.getDrawable().hashCode() == switchOnDrawable.hashCode()) {
                    lockView.setImageDrawable(switchOffDrawable);
                } else {
                    lockView.setImageDrawable(switchOnDrawable);
                }
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            /*ACTION_DOWN????????????????????????
            ACTION_MOVE???????????????????????????
            ACTION_UP???????????????????????????????????????
            ACTION_CANCEL?????????????????????????????????????????????????????????????????????????????????*/
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP: {
                    switch (String.valueOf(view.getTag())) {
                        case TAG_floatWindow:
                            put("floatWindowX", layoutParams.x);
                            put("floatWindowY", layoutParams.y);
                            break;
                        case TAG_floatFunction:
                            put("floatFunctionX", layoutParams.x);
                            put("floatFunctionY", layoutParams.y);
                            break;
                        case TAG_floatMenu:
                            put("floatMenuX", layoutParams.x);
                            put("floatMenuY", layoutParams.y);
                            break;
                        case TAG_floatPlugin:
                            put("floatPluginX", layoutParams.x);
                            put("floatPluginY", layoutParams.y);
                            break;
                        case TAG_floatBrowser:
                            put("floatBrowserX", layoutParams.x);
                            put("floatBrowserY", layoutParams.y);
                            break;
                    }
                    break;
                }
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (lockView == null || lockView.getDrawable().hashCode() == switchOffDrawable.hashCode()) {
                        //?????????
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        layoutParams.x = layoutParams.x + movedX;
                        layoutParams.y = layoutParams.y + movedY;
                        // ???????????????????????????
                        windowManager.updateViewLayout(view, layoutParams);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public class GamePluginListAdapter extends BaseAdapter implements SwitchView.OnCheckedChangeListener {
        private Context context;
        private PluginItem item;
        private List<PluginItem> list;

        GamePluginListAdapter(PluginItem item) {
            this.item = item;
        }

        GamePluginListAdapter(Context context, List<PluginItem> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public void onCheckedChanged(SwitchView switchView, boolean isChecked) {
            try {
                if (GameManager.setScriptEnabled(item.getPath(), isChecked)) {
                    item.setEnable(isChecked);
                    notifyDataSetChanged();
                } else {
                    throw new Exception();
                }
            } catch (Throwable e) {
                switchView.setChecked(!isChecked);
                Toast.makeText(activity, "????????????", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                try {
                    PluginItem item = list.get(position);
                    convertView = LayoutInflater.from(context).inflate(new HelperResources("fmp_float_dialog_menu_plugin_item", HelperResources.RESOURCES_LAYOUT).getLayoutXml(), null);
                    TextView name = convertView.findViewWithTag("name");
                    TextView path = convertView.findViewWithTag("path");
                    TextView size = convertView.findViewWithTag("size");
                    SwitchView enable = convertView.findViewWithTag("enable");

                    if (!TextUtils.isEmpty(item.getName()))
                        name.setText(item.getName());
                    if (!TextUtils.isEmpty(item.getPath()))
                        path.setText(item.getPath().replace(Environment.getExternalStorageDirectory().getAbsolutePath() + "/", ""));
                    size.setText(formatFileSize(item.getSize()));
                    enable.setChecked(item.isEnable());
                    enable.setOnCheckedChangeListener(new GamePluginListAdapter(item));
                    return convertView;
                } catch (Throwable e) {
                    e.printStackTrace();
                    toast("??????????????????");
                    return new View(activity);
                }
            } else {
                return convertView;
            }
        }

        /**
         * ??????????????????
         *
         * @param fileS ??????
         * @return ????????????
         */
        String formatFileSize(long fileS) {
            DecimalFormat df = new DecimalFormat("#.00");
            String fileSizeString;
            String wrongSize = "0B";
            if (fileS == 0) {
                return wrongSize;
            }
            if (fileS < 1024) {
                fileSizeString = df.format((double) fileS) + "B";
            } else if (fileS < 1048576) {
                fileSizeString = df.format((double) fileS / 1024) + "KB";
            } else if (fileS < 1073741824) {
                fileSizeString = df.format((double) fileS / 1048576) + "MB";
            } else {
                fileSizeString = df.format((double) fileS / 1073741824) + "GB";
            }
            return fileSizeString;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
