package com.fmp.helper.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.consumer.ActivitySlidingBackConsumer;
import com.fmp.Logger;
import com.fmp.core.HelperCore;
import com.fmp.helper.adapter.SkinRecycleViewAdapter;
import com.fmp.skins.Skin4DItem;
import com.fmp.skins.SkinItem;
import com.fmp.skins.SkinPackDialog;
import com.fmp.skins.SkinUtil;
import com.fmp.util.FileUtil;
import com.fmp.util.SpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.lwkandroid.imagepicker.ImagePicker;
import com.lwkandroid.imagepicker.data.ImageBean;
import com.lwkandroid.imagepicker.data.ImagePickType;
import com.lwkandroid.imagepicker.utils.GlideImagePickerDisplayer;

import net.fmp.helper.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SkinsActivity extends AppCompatActivity {
    private static final int SKIN_REQUEST_CODE = 500;
    private static final int REQUEST_CODE_CHOOSE = 2000;
    private SkinRecycleViewAdapter skinRecycleViewAdapter;
    private boolean isAdd;
    private RelativeLayout allFab;
    private FloatingActionButton mainFab;
    private List<SkinItem> items = new ArrayList<>();
    private List<Skin4DItem> skin4DItems = new ArrayList<>();
    private RelativeLayout relativeLayout;
    private int fabAnimatorCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_skin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("????????????");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());

        NestedScrollView nestedView = findViewById(R.id.skin_nestedview);
        RecyclerView recyclerView = findViewById(R.id.skin_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        skinRecycleViewAdapter = new SkinRecycleViewAdapter(this, items);
        recyclerView.setAdapter(skinRecycleViewAdapter);

        mainFab = findViewById(R.id.skin_fab);
        mainFab.setOnClickListener(v -> {
            isAdd = !isAdd;
            mainFab.setImageResource(isAdd ? R.drawable.ic_close_white_24dp : R.drawable.ic_add_white_24dp);
            allFab.setVisibility(isAdd ? View.VISIBLE : View.GONE);
            if (isAdd) {
                startAnimator(findViewById(R.id.skin_selector_image));
                startAnimator(findViewById(R.id.skin_selector_pack));
                startAnimator(findViewById(R.id.skin_selector_mcbox));
            }
            fabAnimatorCount = 0;
        });
        allFab = findViewById(R.id.skin_all_fab);
        allFab.setOnClickListener(v -> hideFabMenu());

        FloatingActionButton selectorImageFab = findViewById(R.id.skin_selector_image_fab);
        selectorImageFab.setOnClickListener(v -> {
            hideFabMenu();
            //??????????????????
            new ImagePicker()
                    .pickType(ImagePickType.MULTI) //??????????????????(??????ONLY_CAMERA?????????SINGLE?????????MUTIL)
                    .maxNum(100) //????????????????????????(???????????????????????????????????????????????????1?????????????????????)
                    .needCamera(false) //??????????????????????????????????????????(??????????????????)
                    .cachePath(getCacheDir().getAbsolutePath()) //?????????????????????(????????????????????????????????????)
                    .displayer(new GlideImagePickerDisplayer()) //????????????????????????????????????Glide?????????,???????????????????????????
                    .start(SkinsActivity.this, SKIN_REQUEST_CODE); //?????????RequestCode

        });
        FloatingActionButton selectorPackFab = findViewById(R.id.skin_selector_pack_fab);
        selectorPackFab.setOnClickListener(v -> {
            hideFabMenu();
            //??????????????????
            new LFilePicker()
                    .withActivity(SkinsActivity.this)
                    .withRequestCode(REQUEST_CODE_CHOOSE)
                    .withTitle("????????????")
                    .withIconStyle(Constant.ICON_STYLE_BLUE)
                    .withBackIcon(Constant.BACKICON_STYLETWO)
                    .withMutilyMode(true)
                    .withMaxNum(50)
                    .withStartPath((String) SpUtil.get("FilePickerStartPath", HelperCore.getHelperDirectory().getAbsolutePath()))//????????????????????????
                    .withEndPath(HelperCore.getHelperDirectory().getAbsolutePath())
                    .withNotFoundBooks("????????????????????????")
                    .withChooseMode(true)//?????????????????????
                    .withFileFilter(new String[]{".mcskin"})
                    .start();
        });

        FloatingActionButton selectorMcBox4D = findViewById(R.id.skin_selector_mcbox_fab);
        selectorMcBox4D.setOnClickListener(v -> {
            hideFabMenu();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("??????4D???");
            if (skin4DItems.size() == 0) {
                builder.setMessage("?????????????????????4D????????????");
            } else {
                CharSequence[] charSequences = new CharSequence[skin4DItems.size()];
                for (int i = 0; charSequences.length > i; i++) {
                    charSequences[i] = skin4DItems.get(i).getName();
                }
                builder.setItems(charSequences, (dialog, which) -> {
                    Toasty.info(this, "??????" + charSequences[which], Toast.LENGTH_SHORT, true).show();
                });
            }
            builder.show();
        });

        relativeLayout = findViewById(R.id.not_item_layout);

        //activity????????????
        SmartSwipe.wrap(this)
                .addConsumer(new ActivitySlidingBackConsumer(this))
                //??????????????????
                .setRelativeMoveFactor(0.5F)
                //???????????????????????????????????????enableLeft() ????????????????????????
                .enableLeft()
                .enableRight();
    }

    private void startAnimator(View view) {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(SkinsActivity.this, R.animator.add_bill_anim);
        animatorSet.setTarget(view);
        animatorSet.start();
        animatorSet.setTarget(view);
        animatorSet.setStartDelay(fabAnimatorCount += 200);
    }

    private void hideFabMenu() {
        allFab.setVisibility(View.GONE);
        mainFab.setImageResource(R.drawable.ic_add_white_24dp);
        isAdd = false;
    }

    public void loadSkinPack() {
        File skinPackDir = SkinUtil.getSkinsDir();
        File[] skinPacks = skinPackDir.listFiles();
        if (skinPacks != null) {
            items.clear();
            //????????????
            for (File skinPack : skinPacks) {
                if (skinPack.getName().endsWith(".mcskin")) {
                    //????????????????????????
                    try {
                        //???????????????
                        items.add(SkinUtil.getSkinPack(skinPack.getAbsolutePath()));
                    } catch (IOException e) {
                        Logger.toString(e);
                    }
                }
            }
            relativeLayout.setVisibility(items.size() == 0 ? View.VISIBLE : View.GONE);
            skinRecycleViewAdapter.notifyDataSetChanged();
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void load4DSkin() {
        skin4DItems.clear();
        skin4DItems.addAll(SkinUtil.getSkin4DList());
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSkinPack();
        //load4DSkin();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SkinPackDialog.getInstance().onActivityResult(requestCode, resultCode, data);
        if (requestCode == SKIN_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            //???????????????????????????
            List<ImageBean> resultList = data.getParcelableArrayListExtra(ImagePicker.INTENT_RESULT_DATA);
            if (resultList != null)
                for (ImageBean bean : resultList) {
                    File file = new File(bean.getImagePath());
                    if (file.exists()) {
                        try {
                            SkinUtil.addNewSkinPack(file);
                            Toasty.normal(this, "????????????").show();
                        } catch (IOException e) {
                            Toasty.normal(this, "???????????????" + e.getMessage()).show();
                        }
                    }
                }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE) {
            List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
            if (list != null && list.size() > 0) {
                SpUtil.put("FilePickerStartPath", new File(list.get(0)).getParent());
                for (String str : list) {
                    File fromFile = new File(str);
                    File toFile = new File(SkinUtil.getSkinsDir(), fromFile.getName());
                    if (fromFile.exists() && fromFile.isFile()) {
                        if (toFile.exists()) {
                            Toasty.error(this, String.format("?????????%s??????", fromFile.getName()), Toast.LENGTH_SHORT, true).show();
                        } else {
                            if (FileUtil.copyFile(fromFile.getAbsolutePath(),toFile.getAbsolutePath())) {
                                Toasty.success(this, String.format("??????%s??????", fromFile.getName()), Toast.LENGTH_SHORT, true).show();
                            } else {
                                Toasty.error(this, String.format("?????????%s????????????", fromFile.getName()), Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    } else {
                        Toasty.error(this, String.format("?????????%s????????????", fromFile.getName()), Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
        }
    }
}
