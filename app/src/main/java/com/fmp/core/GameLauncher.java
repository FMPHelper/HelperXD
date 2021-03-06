package com.fmp.core;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fmp.core.http.bean.HelperAccount;
import com.fmp.core.http.bean.HelperPlugin;
import com.fmp.core.push.ClientPush;
import com.fmp.core.push.PushFunction;
import com.fmp.core.push.PushHookCheck;
import com.fmp.data.LauncherSetting;
import com.fmp.dialog.HelperDialog;
import com.fmp.mods.PluginData;
import com.fmp.skins.SkinUtil;
import com.fmp.textures.TextureManager;
import com.fmp.util.FileUtil;
import com.fmp.util.SpUtil;
import com.fmp.util.VeDate;
import com.zzhoujay.richtext.RichText;

import net.fmp.helper.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import per.goweii.anylayer.AnimatorHelper;
import per.goweii.anylayer.AnyLayer;
import per.goweii.anylayer.DragLayout;
import per.goweii.anylayer.Layer;

import static com.fmp.FMP_Tools.IntArrayToString;
import static com.fmp.core.HelperCore.EMPTY_STRING;
import static com.fmp.core.HelperNative.getApplication;

public class GameLauncher {
    public static final String NETEASE_PLAYER_NAME = "SP_NETEASE_PLAYER_NAME";
    public static final String LOAD_MOD_NAME = "SP_LOAD_MOD_NAME";
    public static final int CHECK_START_GAME_ERROR = -1;
    public static final int CHECK_START_GAME_PERMISSIONS = 1;
    public static final int CHECK_START_GAME_SUCCESS = 0;
    private static final int SIGNATURE_HELPER = 310260296;
    private static final int SIGNATURE_NETEASE = 797252355;
    private static final int SIGNATURE_DUOWAN = 1795988125;
    private static final String DUOWAN_PACKAGE_NAME = "com.duowan.groundhog.mctools";
    private static final String CURRENT_MC_VERSION = "SP_CURRENT_MC_VERSION";
    private static GameLauncher launcher;
    private boolean isLaunch = true;
    private boolean removeCache = false;
    private List<ApkInfo> mcTypeList = new ArrayList<>();
    private boolean showRunLog = false;

    private GameLauncher() {
        checkAllVersion();
    }

    public static GameLauncher getInstance() {
        if (launcher == null)
            launcher = new GameLauncher();
        return launcher;
    }

    public void onActivityStart(Activity activity) {
        checkAllVersion();
        File modDir = HelperNative.getApplication().getExternalFilesDir("Mod");
        File nameFile = new File(modDir.getParentFile(), ".PlayerName");
        File startGameFile = new File(modDir, ".StartGame");
        File loadModFile = new File(modDir, ".LoadMod");
        if (startGameFile.exists() || loadModFile.exists()) {
            checkLauncherException(activity, startGameFile, loadModFile);
        }
        if (nameFile.exists()) {
            checkPlayerName(nameFile);
        }
        File runLogFile = new File(modDir, ".RunLog.log");
        if (runLogFile.exists()) {
            checkRunLog(activity, modDir, runLogFile);
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTipMessage(List<String> message) {
        StringBuilder builder = new StringBuilder("????????????????????????<br>");
        for (int i = 0; message.size() > i; i++) {
            builder.append(String.format("%d.%s%s", i + 1, message.get(i), i != message.size() ? "<br>" : EMPTY_STRING));
        }
        return builder.toString();
    }

    private void checkLauncherException(Activity activity, File startGameFile, File loadModFile) {
        File plugin = new File(com.fmp.core.HelperCore.getHelperDirectory(), IntArrayToString(new int[]{82, 127, 117, 131, 128, 122, 117, 64, 63, 123, 132, 64, 129, 63, 117, 114, 133}));//Android/.js/p.dat
        String lastModName = (String) SpUtil.get(LOAD_MOD_NAME, EMPTY_STRING);
        ApkInfo mcInfo = getCurrentVersion();
        ApkInfo dwInfo = new ApkInfo("????????????????????????", DUOWAN_PACKAGE_NAME, "3.2.2", true);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("????????????:%s<br>?????????:%s<br>????????????:%s<br>", mcInfo.getChannelName(), mcInfo.getVersionName(), mcInfo.getSignature() == SIGNATURE_NETEASE ? "??????" : "?????????"));
        builder.append(String.format("????????????:%s<br>????????????:%s<br>", dwInfo.getVersionName(), dwInfo.getSignature() == SIGNATURE_DUOWAN ? "??????" : "?????????"));
        builder.append(String.format("????????????:%s<br>????????????:%s<br><br>", plugin.exists() ? "?????????" : "?????????", TextUtils.isEmpty(lastModName) ? "???" : lastModName));

        List<String> tipMessage = new ArrayList<>();
        if (!mcInfo.getPackageName().equals("com.netease.x19")) {
            tipMessage.add("???????????????<a href=\"http://mc.163.com\">?????????????????????</a>");
        }
        if (mcInfo.getVersionCode() < 840084547) {
            tipMessage.add(String.format("??????<a href=\"http://mc.163.com\">????????????</a>??????????????????%s", mcInfo.getVersionName()));
        }
        if (mcInfo.getSignature() != SIGNATURE_NETEASE) {
            tipMessage.add("???????????????<a href=\"http://mc.163.com\">?????????????????????</a>?????????????????????");
        }
        if (dwInfo.getVersionCode() < 2873) {
            tipMessage.add(String.format("??????<a href=\"http://mcbox.duowan.com/box\">????????????????????????</a>????????????%s", dwInfo.getVersionName()));
        }
        if (mcInfo.getVersionCode() == 840084547 && dwInfo.getVersionCode() < 2873) {
            tipMessage.add("???????????????????????????????????????<a href=\"http://mcbox.duowan.com/box\">????????????????????????</a>");
        }
        if (mcInfo.getVersionCode() < 840084547 && dwInfo.getVersionCode() == 2873) {
            tipMessage.add("???????????????????????????????????????<a href=\"http://mc.163.com\">????????????</a>");
        }
        if (!TextUtils.isEmpty(lastModName)) {
            tipMessage.add(String.format("???????????????????????????????????????????????????????????? <font color='RED'>%s</font>", lastModName));
        }
        if (startGameFile.exists() && mcInfo.getVersionCode() == 840084547 && dwInfo.getVersionCode() == 2873 && TextUtils.isEmpty(lastModName) && plugin.exists() && mcInfo.getPackageName().equals("com.netease.x19")) {
            tipMessage.add("?????????????????????????????????");
            tipMessage.add("??????????????????");
        }
        if (tipMessage.size() == 0) {
            tipMessage.add("????????????");
        }

        builder.append(formatTipMessage(tipMessage));

        HelperDialog dialog = new HelperDialog(activity);
        dialog.setTitle(startGameFile.exists() ? "??????????????????????????????" : "????????????????????????");
        dialog.setMessage(builder.toString());
        RichText.fromHtml(builder.toString()).into(dialog.getMessageView());
        dialog.setButton1("????????????", v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri1 = Uri.fromParts("package", mcInfo.getPackageName(), null);
            intent.setData(uri1);
            activity.startActivity(intent);
        });
        dialog.setButton2("????????????", v -> {
            removeCacheMode();
            SpUtil.remove(LOAD_MOD_NAME);
            dialog.dismiss();
        });
        dialog.setButton3("????????????", v -> {
            startGameFile.delete();
            loadModFile.delete();
            SpUtil.remove(LOAD_MOD_NAME);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void checkPlayerName(File nameFile) {
        try {
            FileInputStream inputStream = new FileInputStream(nameFile);
            byte[] strByte = new byte[inputStream.available()];
            inputStream.read(strByte);
            String playerName = new String(strByte);
            inputStream.close();
            SpUtil.put(NETEASE_PLAYER_NAME, playerName);
            nameFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkRunLog(Activity activity, File modDir, File runLogFile) {
        if (showRunLog) return;
        try {
            FileInputStream inputStream = new FileInputStream(runLogFile);
            byte[] strByte = new byte[inputStream.available()];
            inputStream.read(strByte);
            String logMessage = new String(strByte);
            inputStream.close();
            AnyLayer.dialog(activity)
                    .avoidStatusBar(true)
                    .contentView(R.layout.dialog_notificationl)
                    .gravity(Gravity.TOP)
                    .outsideInterceptTouchEvent(false)
                    .dragDismiss(DragLayout.DragStyle.Top)
                    .onVisibleChangeListener(new Layer.OnVisibleChangeListener() {
                        @Override
                        public void onShow(Layer layer) {
                            ((TextView) layer.getView(R.id.tv_dialog_content)).setText("?????????????????????????????????????????????~");
                            showRunLog = true;
                        }

                        @Override
                        public void onDismiss(Layer layer) {
                            if (runLogFile.exists()) {
                                File cacheLog = new File(modDir.getParentFile().getAbsolutePath(), String.format("ModLog-%s.log", VeDate.getStringDate()));
                                FileUtil.copyFile(runLogFile.getAbsolutePath(), cacheLog.getAbsolutePath());
                                runLogFile.delete();
                                toast("?????????????????????????????????????????????");
                            }
                            showRunLog = false;
                        }
                    })
                    .onClick((layer, v) -> {
                        layer.dismiss();
                        File cacheLog = new File(modDir.getParentFile().getAbsolutePath(), String.format("ModLog-%s.log", VeDate.getStringDate()));
                        FileUtil.copyFile(runLogFile.getAbsolutePath(), cacheLog.getAbsolutePath());
                        runLogFile.delete();
                        AnyLayer.dialog()
                                .contentView(R.layout.dialog_normal_launcher)
                                .backgroundDimDefault()
                                .onClickToDismiss(R.id.fl_dialog_yes)
                                .onVisibleChangeListener(new Layer.OnVisibleChangeListener() {
                                    @Override
                                    public void onShow(Layer layer) {
                                        ((TextView) layer.getView(R.id.tv_dialog_title)).setText(String.format("??????~ ?????????%d???", logMessage.split("\n").length));
                                        ((TextView) layer.getView(R.id.tv_dialog_content)).setText(logMessage);
                                        ((TextView) layer.getView(R.id.tv_dialog_no)).setText("????????????");
                                        ((TextView) layer.getView(R.id.tv_dialog_yes)).setText("?????????");
                                    }

                                    @Override
                                    public void onDismiss(Layer layer) {
                                        toast("?????????????????????????????????????????????");
                                    }
                                })
                                .onClick((layer1, v1) -> {
                                    if (cacheLog.delete()) {
                                        toast("????????????????????????");
                                    }
                                    layer1.dismiss();
                                }, R.id.fl_dialog_no)
                                .show();
                    }, R.id.tv_dialog_content)
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCustomVersion(String packageName) throws CoreException {
        if (TextUtils.isEmpty(packageName))
            throw new NullPointerException("????????????");
        ApkInfo type = new ApkInfo("???????????????", packageName, "??????", true);
        if (!type.isInstall())
            throw new CoreException("????????????");
        if (packageName.equals("com.mojang.minecraftpe"))
            throw new CoreException("???????????????????????????????????????");
        SpUtil.put(CURRENT_MC_VERSION, packageName);
    }

    public ApkInfo getCurrentVersion() {
        String spVersion = (String) SpUtil.get(CURRENT_MC_VERSION, HelperCore.EMPTY_STRING);
        if (!TextUtils.isEmpty(spVersion)) {
            for (ApkInfo type : mcTypeList) {
                if (type.isInstall() && type.getPackageName().equals(spVersion))
                    return type;
            }
            return new ApkInfo("???????????????", spVersion, "??????", true);
        } else {
            for (ApkInfo type : mcTypeList) {
                if (type.isInstall())
                    return type;
            }
        }
        return new ApkInfo("?????????", EMPTY_STRING, EMPTY_STRING, false);
    }

    String getPlayerName() {
        return ((String) SpUtil.get(NETEASE_PLAYER_NAME, EMPTY_STRING)).trim();
    }

    public void selectionLoadMode(Context context, onSelectionLoadMode selectionLoadMode) {
        LauncherSetting setting = LauncherSetting.getSetting();
        String[] items = {
                IntArrayToString(new int[]{36840, 20850, 20040, 30041, 38767, 21165, 36746, 53, 21500, 33034, 23862, 28304, 54}),//?????????????????????(????????????)
                IntArrayToString(new int[]{36841, 20851, 21347, 20168, 28230, 25117, 21166, 36747, 54, 32477, 20870, 27183, 24349, 55}),//????????????????????????(???????????????
                IntArrayToString(new int[]{36839, 20849, 20002, 30040, 21164, 36745, 52, 25436, 36204, 33731, 21474, 53})};//??????????????????(????????????)
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(IntArrayToString(new int[]{35845, 36887, 25335, 36880, 21526, 24758, 30354, 91, 125, 114, 21166, 36747, 26055, 24349}));//?????????????????????Mod????????????
        builder.setSingleChoiceItems(items, setting.getLoadMode(), (dialog, which) -> {
            PushFunction pushFunction = PushFunction.getData();
            int level = HelperCore.getInstance().getUserAbility();
            try {
                switch (which) {
                    case 0: {
                        /*if (pushFunction.isLoadMode1() *//*|| level >= 2*//*) {
                            setting.setLoadMode(which);
                            setting.saveSetting();
                            selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 19973}), IntArrayToString(new int[]{27518, 21178, 36759, 27195, 24361, 21513, 33047, 20276, 23574, 33294, 37122, 21024, 103, 105, 94, 22338, 20053, 30054, 38780, 21178, 36759, 36833, 31269, 20039, 38404, 36890}));//??????????????? //????????????????????????????????????MOD?????????????????????????????????
                            break;
                        } else {
                            throw new CoreException();
                        }*/
                        setting.setLoadMode(which);
                        setting.saveSetting();
                        selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 19973}), IntArrayToString(new int[]{27518, 21178, 36759, 27195, 24361, 21513, 33047, 20276, 23574, 33294, 37122, 21024, 103, 105, 94, 22338, 20053, 30054, 38780, 21178, 36759, 36833, 31269, 20039, 38404, 36890}));//??????????????? //????????????????????????????????????MOD?????????????????????????????????
                        break;
                    }
                    case 1: {
                        /*if (pushFunction.isLoadMode2() *//*|| level >= 2*//*) {
                            setting.setLoadMode(which);
                            setting.saveSetting();
                            selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 20113}), IntArrayToString(new int[]{27513, 21173, 36754, 27190, 24356, 38677, 35222, 24765, 20829, 36848, 20858, 21354, 20175, 28237, 25124, 25186, 21508, 20218, 21173, 36754, 21755}));//??????????????? //???????????????????????????????????????????????????????????????
                            break;
                        } else {
                            throw new CoreException();
                        }*/
                        setting.setLoadMode(which);
                        setting.saveSetting();
                        selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 20113}), IntArrayToString(new int[]{27513, 21173, 36754, 27190, 24356, 38677, 35222, 24765, 20829, 36848, 20858, 21354, 20175, 28237, 25124, 25186, 21508, 20218, 21173, 36754, 21755}));//??????????????? //???????????????????????????????????????????????????????????????
                        break;
                    }
                    case 2: {
                        /*if (pushFunction.isLoadMode3() *//*|| level >= 2*//*) {
                            if (level < -1) {
                                selectionLoadMode.onLoadMode(EMPTY_STRING, IntArrayToString(new int[]{35841, 24754, 20818, 30341, 24415, 36144, 21505, 20887, 20361, 30002})??????????????????????????????);
                            } else if (level == -1) {
                                selectionLoadMode.onLoadMode(EMPTY_STRING, IntArrayToString(new int[]{35841, 24754, 20818, 32475, 23460, 91, 91, 20887, 20361, 30002})???????????????QQ?????????);
                            } else if (level == 0) {
                        selectionLoadMode.onLoadMode(EMPTY_STRING, IntArrayToString(new int[]{25441, 36209, 25497, 26452, 21535, 23618, 21504, 20214, 36890, 25338, 36842, 20027, 21169, 36750, 27186, 24352, 21879}));//???????????????????????????????????????????????????
                    } else {
                                setting.setLoadMode(which);
                                setting.saveSetting();
                                selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 19982}), IntArrayToString(new int[]{36835, 20845, 28224, 25111, 21371, 21495, 21160, 36741}));//??????????????? //????????????????????????
                            }
                            setting.setLoadMode(which);
                            setting.saveSetting();
                            selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 19982}), IntArrayToString(new int[]{36835, 20845, 28224, 25111, 21371, 21495, 21160, 36741}));//??????????????? //????????????????????????
                            break;
                        } else {
                            throw new CoreException();
                        }*/
                        setting.setLoadMode(which);
                        setting.saveSetting();
                        selectionLoadMode.onLoadMode(IntArrayToString(new int[]{21157, 36738, 27174, 24340, 19982}), IntArrayToString(new int[]{36835, 20845, 28224, 25111, 21371, 21495, 21160, 36741}));//??????????????? //????????????????????????
                        break;
                    }
                    default:
                        break;
                }
            } catch (Throwable e) {
                selectionLoadMode.onLoadMode(EMPTY_STRING, "???????????????????????????????????????");
                setting.setLoadMode(-1);
                setting.saveSetting();
            }
            dialog.dismiss();
        });
        builder.show();
    }

    public void selectionMcVersion(Context context, onSelectionVersion selectionVersion) {
        List<ApkInfo> types = new ArrayList<>();
        for (ApkInfo type : mcTypeList) {
            if (type.isInstall()) {
                types.add(type);
            }
        }
        String[] items = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            ApkInfo type = types.get(i);
            items[i] = String.format("%s(%s)", type.getChannelName(), type.getVersionName());
        }
        int checkedItem = 0;
        String spVersion = (String) SpUtil.get(CURRENT_MC_VERSION, HelperCore.EMPTY_STRING);
        if (!TextUtils.isEmpty(spVersion)) {
            for (int i = 0; types.size() > i; i++) {
                if (types.get(i).getPackageName().equals(spVersion)) {
                    checkedItem = i;
                    break;
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("????????????????????????");
        builder.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            ApkInfo type = types.get(which);
            toast(String.format("?????????%s%s%s", type.getChannelName(), type.isSupportStart() ? HelperCore.EMPTY_STRING : "\n?????????????????????????????????", type.getSignature() == SIGNATURE_NETEASE ? HelperCore.EMPTY_STRING : "\n???????????????????????????"));
            SpUtil.put(CURRENT_MC_VERSION, type.getPackageName());
            selectionVersion.onType(type);
        });
        builder.setNegativeButton(context.getString(R.string.launcher_goto_netease), (dialog, which) -> {
            Uri uri = Uri.parse("http://mc.163.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        });
        builder.show();
    }

    private void toast(String msg) {
        Random mRandom = new Random();
        AnyLayer.toast()
                .duration(3000)
                .icon(R.drawable.ic_extension_white_24dp)
                .message(msg)
                .alpha(mRandom.nextFloat())
                .backgroundColorInt(Color.argb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)))
                .gravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
                .animator(new Layer.AnimatorCreator() {
                    @Override
                    public Animator createInAnimator(View target) {
                        return AnimatorHelper.createZoomAlphaInAnim(target);
                    }

                    @Override
                    public Animator createOutAnimator(View target) {
                        return AnimatorHelper.createZoomAlphaOutAnim(target);
                    }
                })
                .show();
    }

    private void checkAllVersion() {
        mcTypeList.clear();
        Application application = HelperNative.getApplication();
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_1), "com.netease.x19", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_2), "com.netease.mc.m4399", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_3), "com.netease.mc.huawei", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_4), "com.tencent.tmgp.wdsj666", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_5), "com.netease.mc.baidu", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_6), "com.netease.mc.mi", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_7), "com.netease.mc.aligames", "1.16", true));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_8), "com.netease.mc.nearme.gamecenter", "1.16", false));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_9), "com.netease.mc.vivo", "1.16", false));
        mcTypeList.add(new ApkInfo(application.getString(R.string.launcher_package_name_10), "com.netease.mc.bili", "1.16", true));
    }

    public void removeCacheMode() {
        removeCache = true;
        File file = new File(com.fmp.core.HelperCore.getHelperDirectory(), "mctools");
        deleteDir(file);
    }

    private void deleteDir(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                //????????????
                file.delete();
            } else if (file.isDirectory()) {
                //???????????????
                File[] files = file.listFiles();
                if (files != null) {
                    for (File list : files) {
                        if (list.isFile()) {
                            //????????????
                            list.delete();
                        } else if (list.isDirectory()) {
                            //????????????
                            deleteDir(list);
                        }
                    }
                }
                //???????????????????????????????????????????????????????????????/??????
                file.delete();
            }
        }
    }

    public void stopStartGame() {
        isLaunch = false;
    }

    public void checkStartGame(onCheckStartGame checkStartGame) {
        /*int level = HelperCore.getInstance().getUserAbility();
        if (level < -1) {
            checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{35841, 24754, 20818, 30341, 24415, 36144, 21505, 20887, 20361, 30002})*//*??????????????????????????????*//*), false, CHECK_START_GAME_PERMISSIONS);
        } else if (level == -1) {
            checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{35843, 24756, 20820, 32477, 23462, 93, 93, 20889, 21563, 21172, 28228, 25115})*//*???????????????QQ???????????????*//*), false, CHECK_START_GAME_PERMISSIONS);
        } else {*/
            try {
                //????????????packageManager
                PackageManager packageManager = HelperNative.getApplication().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(DUOWAN_PACKAGE_NAME, PackageManager.GET_META_DATA);
                File libDirFile = new File(applicationInfo.nativeLibraryDir);
                File tinysubstrate = new File(libDirFile, IntArrayToString(new int[]{140, 137, 130, 141, 131, 144, 133, 140, 129, 149, 142, 131, 136, 133, 146, 127, 148, 137, 142, 153, 147, 149, 130, 147, 148, 146, 129, 148, 133, 78, 147, 143}));//libmcpelauncher_tinysubstrate.so
                File neteasy = new File(libDirFile, IntArrayToString(new int[]{134, 131, 124, 135, 125, 138, 127, 134, 123, 143, 136, 125, 130, 127, 140, 121, 136, 127, 142, 127, 123, 141, 147, 72, 141, 137}));//"libmcpelauncher_neteasy.so
                if (!tinysubstrate.exists() || !neteasy.exists()) {
                    checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{23452, 35032, 21272, 25458, 22370, 65311, 35850, 37344, 26051, 23452, 35032, 22829, 29628, 25124, 30359, 20009, 30047, 30437, 23395})), false, CHECK_START_GAME_ERROR);//???????????????????????????????????????
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{22821, 29620, 25116, 30351, 20001, 30039, 30429, 23387, 26421, 23444, 35024})), false, CHECK_START_GAME_ERROR);//?????????????????????????????????
                return;
            }
            /*File plugin = new File(com.fmp.core.HelperCore.getHelperDirectory(), IntArrayToString(new int[]{82, 127, 117, 131, 128, 122, 117, 64, 63, 123, 132, 64, 129, 63, 117, 114, 133}));//Android/.js/p.dat
            if (!plugin.exists()) {
                checkStartGame.onStart(new CoreException("???????????????????????????"), false);
                return;
            }*/
            ApkInfo type = getCurrentVersion();
            if (type == null) {
                checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{26417, 35781, 32629, 21558, 21167, 29263, 26419})), false, CHECK_START_GAME_ERROR);//?????????????????????
                return;
            }
            try {
                new HelperPluginManager().outPutPluginFile();
            } catch (Throwable e) {
                checkStartGame.onStart(new CoreException(IntArrayToString(new int[]{26700, 24535, 26376, 26052, 22853, 36153, 65312, 24764, 21507, 33041, 26100, 27881, 27511, 24140, 20371, 30012, 21172, 36753, 21171, 33041})), true, CHECK_START_GAME_ERROR);//????????????????????????????????????????????????????????????
            }
            LauncherSetting setting = LauncherSetting.getSetting();
            if (TextUtils.isEmpty(setting.getCoreRequest())) {
                ClientPush.getInstance().checkRequestCore(new ClientPush.onRequestCoreListener() {
                    @Override
                    public void done(CoreException e, String coreUrl) {
                        if (e == null) {
                            setting.setCoreRequest(coreUrl);
                            setting.saveSetting();
                        } else {
                            checkStartGame.onStart(new CoreException(e), false, CHECK_START_GAME_ERROR);
                        }
                    }
                });
            }
            try {
                //????????????
                ((ActivityManager) HelperNative.getApplication().getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(type.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (isLaunch) {
                    File modDir = GamePluginManager.getInstance().getModFilesDir();
                    try {
                        new File(modDir, ".StartGame").createNewFile();
                        new File(modDir, ".LoadMod").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    LauncherSetting mSetting = LauncherSetting.getSetting();
                    try {
                        HelperNative.startNetEaseMc(type.getPackageName(), String.format(mSetting.getCoreRequest(), mSetting.getMcboxId(), mSetting.getMcboxId(), System.currentTimeMillis()), mSetting.isSafeMode() ? EMPTY_STRING : getModData(), getGameMods(), DeviceIdUtil.getDeviceId(), mSetting.getLoadMode(), mSetting.onMudwFloat(), mSetting.onGameFloat(), getSkinData(), getTextureData(), removeCache, getHookCheck());
                        checkStartGame.onStart(null, true, CHECK_START_GAME_SUCCESS);
                        if (mSetting.isSafeMode())
                            Toasty.normal(HelperNative.getApplication(), IntArrayToString(new int[]{24421, 21087, 20044, 23451, 20858, 27187, 24353, 21569, 21178, 65310, 19999, 20268, 21170, 36751, 20237, 20327, 25572, 20232}), R.drawable.ic_extension_white_24dp).show();//??????????????????????????????????????????????????????
                    } catch (Throwable e) {
                        checkStartGame.onStart(new CoreException(e.getMessage()), false, CHECK_START_GAME_ERROR);
                    }
                    removeCache = false;
                } else {
                    isLaunch = true;
                    checkStartGame.onStart(null, false, CHECK_START_GAME_ERROR);
                }
            }, 1000 * 3);
        //}
    }

    private String getHookCheck() {
        PushHookCheck hookCheck = PushHookCheck.getData();
        if (hookCheck.getCallPython() != null && hookCheck.getCallPython().size() != 0) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String check : hookCheck.getCallPython()) {
                if (i >= 1) {
                    builder.append("~");
                }
                builder.append(check);
                i++;
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    private String getGameMods() {
        List<HelperPlugin> modList = GamePluginManager.getInstance().getAllMods();
        HelperAccount userData = HelperCore.getInstance().getUserData();
        if (modList.size() != 0 && userData != null && userData.gameMods != null && userData.gameMods.size() > 0) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (HelperPlugin plugin : modList) {
                if (plugin.api) {
                    for (int id : userData.gameMods) {
                        if (id == plugin.id) {
                            if (i >= 1) {
                                builder.append(",");
                            }
                            builder.append(plugin.id);
                            i++;
                            break;
                        }
                    }

                }
            }
            return builder.toString();
        }
        return null;
    }

    private String getModData() {
        try {
            StringBuilder loadMods = new StringBuilder();
            List<PluginData.Item> items = LocalPluginManager.getInstance().getEnablePluginList();
            for (int i = 0, i2 = 0; items.size() > i; i++) {
                //???????????????????????????1
                if (i2 >= 1) {
                    //???????????????
                    loadMods.append(",");
                }
                //??????MOD?????????
                int key = GamePluginManager.getInstance().getModKey(items.get(i).getName());
                if (key != 50) {
                    //??????key
                    loadMods.append(key);
                    //???????????????
                    loadMods.append("'");
                }
                loadMods.append(items.get(i).getPath());
                i2++;
            }
            return loadMods.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSkinData() {
        String curSkin = SkinUtil.getCurSkinName();
        if (!TextUtils.isEmpty(curSkin)) {
            File skinFile = new File(SkinUtil.getSkinsDir(), curSkin);
            if (skinFile.exists())
                return skinFile.getAbsolutePath();
            return null;
        } else {
            return null;
        }
    }

    private String getTextureData() {
        try {
            StringBuilder builder = new StringBuilder();
            List<TextureManager.Item> items = TextureManager.getInstance().getTexturesList();
            if (items != null) {
                for (int i = 0, i2 = 0; items.size() > i; i++) {
                    //??????????????????
                    if (items.get(i).isEnable()) {
                        //???????????????????????????1
                        if (i2 >= 1) {
                            //???????????????
                            builder.append(",");
                        }
                        builder.append(items.get(i).getPath());
                        i2++;
                    }
                }
                if (builder.length() == 0)
                    return null;
                return builder.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /*private boolean writeSupportFile() {
        File soFile = new File(com.fmp.core.HelperCore.getHelperDirectory(), IntArrayToString(new int[]{82, 127, 117, 131, 128, 122, 117, 64, 63, 123, 132, 64, 129, 63, 117, 114, 133}));//Android/.js/p.dat
        File parentFile = new File(com.fmp.core.HelperCore.getHelperDirectory(), IntArrayToString(new int[]{76, 121, 111, 125, 122, 116, 111, 58, 57, 117, 126}));//Android/.js
        if (!parentFile.exists())
            parentFile.mkdirs();
        else if (!parentFile.isDirectory() && parentFile.canWrite()) {
            parentFile.delete();
            parentFile.mkdirs();
        } else {
            parentFile.delete();
            parentFile.mkdir();
        }
        try {
            ZipFile zipFile = new ZipFile(getApplication().getPackageResourcePath());
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(IntArrayToString(new int[]{141, 138, 131, 80, 130, 147, 142, 134, 130, 131, 138, 78, 151, 88, 130, 80, 141, 138, 131, 135, 142, 145, 78, 148, 150, 145, 145, 144, 147, 149, 79, 148, 144}*//*lib/armeabi-v7a/libfmp-support.so*//*)));
            // ????????????????????????
            FileOutputStream out = new FileOutputStream(soFile);
            int len;
            byte[] buffer = new byte[1024];
            // ????????????????????????
            while ((len = inputStream.read(buffer)) != -1) {
                // ??????????????????????????????
                out.write(buffer, 0, len);
                //???????????????
                out.flush();
            }
            //???????????????
            out.close();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }*/

    public interface onCheckStartGame {
        void onStart(CoreException e, boolean start, int callBackCoee);
    }

    public interface onSelectionVersion {
        void onType(ApkInfo type);
    }

    public interface onSelectionLoadMode {
        void onLoadMode(String loadModeName, String message);
    }

    public class ApkInfo {
        private boolean isInstall;
        private String channelName;
        private String versionName;
        private int versionCode;
        private String packageName;
        private String supportVersion;
        private int signature;
        private boolean supportStart;

        @SuppressLint({"WrongConstant", "PackageManagerGetSignatures"})
        ApkInfo(String channelName, String packageName, String supportVersion, boolean supportStart) {
            this.channelName = channelName;
            this.packageName = packageName;
            this.supportVersion = supportVersion;
            this.supportStart = supportStart;
            try {
                PackageInfo info = getApplication().getPackageManager().getPackageInfo(packageName, 64);
                this.versionName = info.versionName;
                this.versionCode = info.versionCode;
                this.signature = info.signatures[0].hashCode();
                this.isInstall = true;
            } catch (Throwable e) {
                this.versionName = "";
                this.signature = 0;
                this.versionCode = 0;
                this.isInstall = false;
            }
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public boolean isInstall() {
            return isInstall;
        }

        public void setInstall(boolean install) {
            isInstall = install;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getSupportVersion() {
            return supportVersion;
        }

        public void setSupportVersion(String supportVersion) {
            this.supportVersion = supportVersion;
        }

        public int getSignature() {
            return signature;
        }

        public void setSignature(int signature) {
            this.signature = signature;
        }

        public boolean isSupportStart() {
            return supportStart;
        }

        public void setSupportStart(boolean supportStart) {
            this.supportStart = supportStart;
        }
    }

}
