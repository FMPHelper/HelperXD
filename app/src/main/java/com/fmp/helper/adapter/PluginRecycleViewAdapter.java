package com.fmp.helper.adapter;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fmp.FMP_Tools;
import com.fmp.UploadHelper;
import com.fmp.core.CoreException;
import com.fmp.core.GamePluginManager;
import com.fmp.core.HelperCore;
import com.fmp.core.http.bean.HelperAccount;
import com.fmp.core.http.bean.HelperPlugin;
import com.fmp.util.FileSizeUtil;
import com.fmp.util.SpUtil;
import com.fmp.view.CircleImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.lwkandroid.imagepicker.ImagePicker;
import com.lwkandroid.imagepicker.data.ImageBean;
import com.lwkandroid.imagepicker.data.ImagePickType;
import com.lwkandroid.imagepicker.utils.GlideImagePickerDisplayer;
import com.squareup.picasso.Picasso;

import net.fmp.helper.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;
import per.goweii.anylayer.AnimatorHelper;
import per.goweii.anylayer.AnyLayer;
import per.goweii.anylayer.DialogLayer;
import per.goweii.anylayer.Layer;

import static android.app.Activity.RESULT_OK;

public class PluginRecycleViewAdapter extends RecyclerView.Adapter<PluginRecycleViewAdapter.MyViewHolder> {
    private static final int PORTRAIT_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_CHOOSE = 2000;
    private AppCompatActivity activity;
    private List<HelperPlugin> userMods;
    private onUploadIcon uploadIcon;
    private onUploadMod uploadMod;
    private Integer mKey;

    public PluginRecycleViewAdapter(AppCompatActivity context) {
        this.activity = context;
        this.userMods = new ArrayList<>();
        List<HelperPlugin> list = GamePluginManager.getInstance().getAllMods();
        HelperAccount userData = HelperCore.getInstance().getUserData();
        if (userData != null && userData.plugins != null && list.size() != 0) {
            for (HelperPlugin mod : list) {
                for (int id:userData.plugins){
                    if (id==mod.id || id==-1){
                        userMods.add(mod);
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_layout_plugin_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull MyViewHolder holder, int position) {
        HelperPlugin plugin = userMods.get(position);
        holder.cardView.setOnClickListener(v -> showDialog(plugin));
        if (!TextUtils.isEmpty(plugin.icon)) {
            Picasso.get()
                    .load(plugin.icon)
                    .into(holder.Icon);
        }
        holder.Name.setText(plugin.name);
        if (!TextUtils.isEmpty(plugin.size)) {
            holder.Size.setText(FileSizeUtil.FormetFileSize(Long.parseLong(plugin.size)));
        }
        setType(holder.Type, plugin.type);
        holder.Code.setText(plugin.code);
        holder.Info.setText(plugin.info);
    }

    @Override
    public int getItemCount() {
        return userMods.size();
    }

    private void showDialog(HelperPlugin plugin) {
        HelperPlugin editPlugin = new HelperPlugin();
        AnyLayer.dialog(activity)
                .contentView(R.layout.dialog_edit_plugin)
                .backgroundDimDefault()
                .contentAnimator(new DialogLayer.AnimatorCreator() {
                    @Override
                    public Animator createInAnimator(View content) {
                        return AnimatorHelper.createTopAlphaInAnim(content);
                    }

                    @Override
                    public Animator createOutAnimator(View content) {
                        return AnimatorHelper.createTopAlphaOutAnim(content);
                    }
                })
                .onVisibleChangeListener(new Layer.OnVisibleChangeListener() {
                    @Override
                    public void onShow(Layer layer) {
                        ((TextView) layer.getView(R.id.tv_dialog_title)).setText(String.format("????????????-%s", plugin.name));
                        ((TextView) layer.getView(R.id.tv_dialog_no)).setText("????????????");
                        ((TextView) layer.getView(R.id.tv_dialog_yes)).setText("????????????");
                        if (!TextUtils.isEmpty(plugin.icon)) {
                            Picasso.get()
                                    .load(plugin.icon)
                                    .into((CircleImageView) layer.getView(R.id.plugin_edit_icon));
                        }
                        ((FancyButton) layer.getView(R.id.plugin_edit_upload)).setText("????????????");
                        ((TextView) layer.getView(R.id.plugin_edit_type_tip)).setText("????????????");
                        ((TextInputEditText) layer.getView(R.id.plugin_edit_name)).setText(plugin.name);
                        ((TextInputEditText) layer.getView(R.id.plugin_edit_code)).setText(plugin.code);
                        ((TextInputEditText) layer.getView(R.id.plugin_edit_info)).setText(plugin.info);
                        ((TextInputEditText) layer.getView(R.id.plugin_edit_group_key)).setText(plugin.groupKey);
                        addTextChangedListener(layer.getView(R.id.plugin_edit_name_input), layer.getView(R.id.plugin_edit_name), 20);
                        addTextChangedListener(layer.getView(R.id.plugin_edit_code_input), layer.getView(R.id.plugin_edit_code), 10);
                        addTextChangedListener(layer.getView(R.id.plugin_edit_info_input), layer.getView(R.id.plugin_edit_info), 100);
                        addTextChangedListener(layer.getView(R.id.plugin_edit_group_key_input), layer.getView(R.id.plugin_edit_group_key), 35);
                        ((RadioButton) layer.getView(R.id.plugin_edit_type_0)).setText("??????");
                        ((RadioButton) layer.getView(R.id.plugin_edit_type_1)).setText("??????");
                        ((RadioButton) layer.getView(R.id.plugin_edit_type_2)).setText("??????");
                        ((RadioButton) layer.getView(R.id.plugin_edit_type_3)).setText("??????");
                            switch (plugin.type) {
                                case 0:
                                    ((RadioButton) layer.getView(R.id.plugin_edit_type_0)).setChecked(true);
                                    break;
                                case 1:
                                    ((RadioButton) layer.getView(R.id.plugin_edit_type_1)).setChecked(true);
                                    break;
                                case 2:
                                    ((RadioButton) layer.getView(R.id.plugin_edit_type_2)).setChecked(true);
                                    break;
                                case 3:
                                    ((RadioButton) layer.getView(R.id.plugin_edit_type_3)).setChecked(true);
                                    break;
                                default:
                                    break;
                            }
                    }

                    @Override
                    public void onDismiss(Layer layer) {
                    }
                })
                .onClick((layer, v) -> uploadIcon(plugin, (e, url) -> {
                    if (e == null) {
                        editPlugin.icon=url;
                        Picasso.get()
                                .load(url)
                                .into((CircleImageView) layer.getView(R.id.plugin_edit_icon));
                        Toasty.normal(activity, "??????????????????", R.drawable.ic_extension_white_24dp).show();
                    } else {
                        Toasty.normal(activity, "?????????????????????" + e.getMessage(), R.drawable.ic_extension_white_24dp).show();
                    }
                }), R.id.plugin_edit_icon)
                .onClick((layer, v) -> uploadMod(plugin, (e, url, file) -> {
                    if (e == null) {
                        editPlugin.size=String.valueOf(file.length());
                        editPlugin.url=url;
                        Toasty.normal(activity, "??????????????????", R.drawable.ic_extension_white_24dp).show();
                    } else {
                        Toasty.normal(activity, "?????????????????????" + e.getMessage(), R.drawable.ic_extension_white_24dp).show();
                    }
                }), R.id.plugin_edit_upload)
                .onClick((layer, v) -> {
                    Editable name = ((TextInputEditText) layer.getView(R.id.plugin_edit_name)).getText();
                    Editable code = ((TextInputEditText) layer.getView(R.id.plugin_edit_code)).getText();
                    Editable info = ((TextInputEditText) layer.getView(R.id.plugin_edit_info)).getText();
                    Editable groupKey = ((TextInputEditText) layer.getView(R.id.plugin_edit_group_key)).getText();

                    if (name != null && !TextUtils.isEmpty(name)) {
                        if (name.toString().length() > 20) {
                            toast("????????????????????????");
                            return;
                        } else {
                            editPlugin.name=name.toString().trim();
                        }
                    }
                    if (code != null && !TextUtils.isEmpty(code)) {
                        if (code.toString().length() > 10) {
                            toast("???????????????????????????");
                            return;
                        } else {
                            editPlugin.code=code.toString().trim();
                        }
                    }
                    if (info != null && !TextUtils.isEmpty(info)) {
                        if (info.toString().length() > 100) {
                            toast("????????????????????????");
                            return;
                        } else {
                            editPlugin.info=info.toString().trim();
                        }
                    }
                    if (groupKey != null && !TextUtils.isEmpty(groupKey)) {
                        if (groupKey.toString().length() > 35) {
                            toast("Key??????????????????");
                            return;
                        } else {
                            editPlugin.groupKey=groupKey.toString().trim();
                        }
                    }

                    if (((RadioButton) layer.getView(R.id.plugin_edit_type_0)).isChecked()) {
                        editPlugin.type=0;
                    }
                    if (((RadioButton) layer.getView(R.id.plugin_edit_type_1)).isChecked()) {
                        editPlugin.type=1;
                    }
                    if (((RadioButton) layer.getView(R.id.plugin_edit_type_2)).isChecked()) {
                        editPlugin.type=2;
                    }
                    if (((RadioButton) layer.getView(R.id.plugin_edit_type_3)).isChecked()) {
                        editPlugin.type=3;
                    }
                    editPlugin.objectId=plugin.objectId;
                    editPlugin.updatePlugin(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                layer.dismiss();
                                if (!TextUtils.isEmpty(editPlugin.icon))
                                    plugin.icon=(editPlugin.icon);
                                if (!TextUtils.isEmpty(editPlugin.size))
                                    plugin.size=(editPlugin.size);
                                if (!TextUtils.isEmpty(editPlugin.url))
                                    plugin.url=(editPlugin.url);
                                plugin.name=(editPlugin.name);
                                plugin.code=(editPlugin.code);
                                plugin.info=(editPlugin.info);
                                plugin.groupKey=(editPlugin.groupKey);
                                plugin.type=(editPlugin.type);
                                notifyDataSetChanged();
                                toast("??????????????????");
                            } else {
                                toast("???????????????" + new CoreException(e).getMessage());
                            }
                        }
                    });
                }, R.id.fl_dialog_yes)
                .onClickToDismiss(R.id.fl_dialog_no)
                .show();
    }

    @SuppressLint("DefaultLocale")
    private void addTextChangedListener(TextInputLayout layout, TextInputEditText editText, int maxCount) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (start > maxCount) {
                    layout.setError(String.format("??????%d?????????", maxCount));
                    layout.setErrorEnabled(true);
                } else {
                    layout.setError("");
                    layout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    private void uploadIcon(HelperPlugin plugin, onUploadIcon listener) {
        uploadIcon = listener;
        //??????????????????
        new ImagePicker()
                .pickType(ImagePickType.SINGLE) //??????????????????(??????ONLY_CAMERA?????????SINGLE?????????MUTIL)
                .maxNum(1) //????????????????????????(???????????????????????????????????????????????????1?????????????????????)
                .needCamera(true) //??????????????????????????????????????????(??????????????????)
                .cachePath(activity.getCacheDir().getAbsolutePath()) //?????????????????????(????????????????????????????????????)
                .displayer(new GlideImagePickerDisplayer()) //????????????????????????????????????Glide?????????,???????????????????????????
                .start(activity, PORTRAIT_REQUEST_CODE); //?????????RequestCode
    }

    private void uploadMod(HelperPlugin plugin, onUploadMod listener) {
        uploadMod = listener;
        mKey = plugin.key;
        new LFilePicker()
                .withActivity(activity)
                .withRequestCode(REQUEST_CODE_CHOOSE)
                .withTitle("????????????")
                .withIconStyle(Constant.ICON_STYLE_BLUE)
                .withBackIcon(Constant.BACKICON_STYLETWO)
                .withMutilyMode(false)
                .withMaxNum(50)
                .withStartPath((String) SpUtil.get("FilePickerStartPath", HelperCore.getHelperDirectory().getAbsolutePath()))//????????????????????????
                .withEndPath(HelperCore.getHelperDirectory().getAbsolutePath())
                .withNotFoundBooks("????????????????????????")
                .withChooseMode(true)//?????????????????????
                .withFileFilter(new String[]{".js", ".modpkg", ".fmod", ".nmod"})
                .start();
    }

    private void showModEncryptModeDialog(String path) {
        String[] items = {"????????????", "?????????fmod", "?????????????????????"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("??????????????????");
        builder.setSingleChoiceItems(items, -1, (dialog, which) -> {
            switch (which) {
                case 0: {
                    try {
                        File file = new File(path);
                        uploadMod.onUpload(null, UploadHelper.uploadMod(path), file);
                    } catch (Exception ex) {
                        uploadMod.onUpload(new CoreException(ex.getMessage()), null, null);
                    }
                    break;
                }
                case 1: {
                    if (mKey == null || mKey == -1) {
                        uploadMod.onUpload(new CoreException("??????????????????????????????"), null, null);
                        return;
                    }
                    if (path.endsWith(".fmod")){
                        uploadMod.onUpload(new CoreException("?????????????????????????????????????????????"), null, null);
                        return;
                    }
                    try {
                        File file = new File(FMP_Tools.encryptFile(mKey, path));
                        uploadMod.onUpload(null, UploadHelper.uploadMod(file.getAbsolutePath()), file);
                        file.delete();
                    } catch (Exception ex) {
                        uploadMod.onUpload(new CoreException(ex.getMessage()), null, null);
                    }
                    mKey = null;
                    break;
                }
                case 2: {
                    if (mKey == null || mKey == -1) {
                        uploadMod.onUpload(new CoreException("??????????????????????????????"), null, null);
                        return;
                    }
                    if (path.endsWith(".fmod")){
                        uploadMod.onUpload(new CoreException("?????????????????????????????????????????????"), null, null);
                        return;
                    }
                    try {
                        File file = new File(FMP_Tools.encryptFileByApi(mKey, path));
                        uploadMod.onUpload(null, UploadHelper.uploadMod(file.getAbsolutePath()), file);
                        file.delete();
                        notifyDataSetChanged();
                    } catch (Exception ex) {
                        uploadMod.onUpload(new CoreException(ex.getMessage()), null, null);
                    }
                    mKey = null;
                    break;
                }
            }
            dialog.dismiss();
        });
        builder.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE) {
            List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
            if (list != null && list.size() > 0) {
                SpUtil.put("FilePickerStartPath", new File(list.get(0)).getParent());
                if (!TextUtils.isEmpty(list.get(0))) {
                    showModEncryptModeDialog(list.get(0));
                } else {
                    uploadMod.onUpload(new CoreException("??????????????????"), null, null);
                }
            }
        }
        if (requestCode == PORTRAIT_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            //???????????????????????????
            List<ImageBean> resultList = data.getParcelableArrayListExtra(ImagePicker.INTENT_RESULT_DATA);
            if (resultList != null) {
                try {
                    File file = new File(resultList.get(0).getImagePath());
                    if (file.length() < 1024 * 1024 * 4) {
                        uploadIcon.onUpload(null, UploadHelper.uploadIcon(file.getAbsolutePath()));
                    } else {
                        uploadIcon.onUpload(new CoreException("??????????????????4MB"), null);
                    }
                } catch (Exception e) {
                    uploadIcon.onUpload(new CoreException("??????????????????"), null);
                }
            } else {
                uploadIcon.onUpload(new CoreException("??????????????????"), null);
            }
        }
    }

    private void setType(TextView tv, Integer type) {
        if (type != null) {
            if (type == 0) {
                tv.setText("??????");
                tv.setTextColor(Color.GREEN);
            } else if (type == 1) {
                tv.setText("??????");
                tv.setTextColor(Color.RED);
            } else if (type == 2) {
                tv.setText("??????");
                tv.setTextColor(Color.MAGENTA);
            } else {
                tv.setText("??????");
                tv.setTextColor(Color.BLACK);
            }
            return;
        }
        tv.setText("??????");
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

    private interface onUploadIcon {
        void onUpload(CoreException e, String url);
    }

    private interface onUploadMod {
        void onUpload(CoreException e, String url, File file);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private CircleImageView Icon;
        private TextView Name;
        private TextView Size;
        private TextView Type;
        private TextView Code;
        private TextView Info;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.plugin_cardview);
            Icon = itemView.findViewById(R.id.plugin_icon);
            Name = itemView.findViewById(R.id.plugin_name);
            Size = itemView.findViewById(R.id.plugin_size);
            Type = itemView.findViewById(R.id.plugin_type);
            Code = itemView.findViewById(R.id.plugin_code);
            Info = itemView.findViewById(R.id.plugin_info);
        }
    }
}
