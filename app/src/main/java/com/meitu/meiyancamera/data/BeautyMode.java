package com.meitu.meiyancamera.data;

import android.content.Context;
import android.content.res.Resources;

import com.meitu.meiyancamera.R;

/**
 * 枚举了所有美颜模式
 * Created by libiao on 2017/6/6.
 */
public enum BeautyMode {

    YUAN_TU(0, 0, 100, R.string.beauty_mode_yuantu, "原图", R.color.colorModeGray),
    TIAN_MEI_KE_REN(1, 118, 80, R.string.beauty_mode_tianmeikeren, "甜美可人", R.color.colorModePink),
    ZI_RAN(2, 116, 80, R.string.beauty_mode_ziran, "自然", R.color.colorModeOrange),
    HUA_YAN(3, 480, 80, R.string.beauty_mode_huayan, "花颜", R.color.colorModeRed),
    FEN_DAI(4, 553, 80, R.string.beauty_mode_fendai, "粉黛", R.color.colorModePink),
    CHU_XIA(5, 477, 80, R.string.beauty_mode_chuxia, "初夏", R.color.colorModeGreen),
    LAN_DIAO(6, 161, 80, R.string.beauty_mode_landiao, "蓝调", R.color.colorModePink),
    WEI_MEI(7, 124, 80, R.string.beauty_mode_weimei, "唯美", R.color.colorModeOrange),
    YING_CAI(8, 357, 80, R.string.beauty_mode_yingcai, "萤彩", R.color.colorModePink),
    XIN_XUE(9, 501, 80, R.string.beauty_mode_xinxue, "新雪", R.color.colorModeRed),
    LUO_KE_KE(10, 283, 80, R.string.beauty_mode_luokeke, "洛可可", R.color.colorModeOrange),
    FEI_YAN(11, 389, 80, R.string.beauty_mode_feiyan, "霏颜", R.color.colorModePink),
    MI_YOU(12, 505, 80, R.string.beauty_mode_miyou, "蜜柚", R.color.colorModeOrange),
    TIAN_DAN(13, 358, 80, R.string.beauty_mode_tiandan, "恬淡", R.color.colorModePink),
    BAI_LU(14, 175, 80, R.string.beauty_mode_bailu, "白露", R.color.colorModeGreen),
    YUE_GUANG(15, 284, 80, R.string.beauty_mode_yueguang, "月光", R.color.colorModeOrange),
    FEN_NEN_XI(16, 120, 80, R.string.beauty_mode_fennenxi, "粉嫩系", R.color.colorModePink),
    ROU_GUANG(17, 122, 80, R.string.beauty_mode_rouguang, "柔光", R.color.colorModeShallowBlack),
    QING_LIANG(18, 130, 80, R.string.beauty_mode_qingliang, "清凉", R.color.colorModeGray),
    RI_XI(19, 126, 80, R.string.beauty_mode_rixi, "日系", R.color.colorModeRed),
    A_BAO_SE(20, 132, 80, R.string.beauty_mode_abaose, "阿宝色", R.color.colorModePink),
    DIAN_YA(21, 160, 80, R.string.beauty_mode_dianya, "典雅", R.color.colorModeOrange),
    MEI_HUO(22, 360, 80, R.string.beauty_mode_meihuo, "魅惑", R.color.colorModeRed),
    LIU_DING(23, 359, 80, R.string.beauty_mode_liuding, "柳丁", R.color.colorModeGreen),
    MI_HUAN(24, 361, 80, R.string.beauty_mode_mihuan, "迷幻", R.color.colorModeOrange),
    HEI_BAI(25, 113, 80, R.string.beauty_mode_heibai, "黑白", R.color.colorModeShallowBlack);

    //_id
    private final int mId;
    //滤镜
    private final int mFilterId;
    //默认透明度
    private final int mAlpha;
    //文字资源ID
    private final int mResId;
    //美颜模式描述
    private final String mDescrible;
    //美颜模式对应的颜色
    private final int mColor;

    BeautyMode(int id, int filterId, int alpha, int resId, String describle, int color){
        this.mId = id;
        this.mFilterId = filterId;
        this.mAlpha = alpha;
        this.mResId = resId;
        this.mDescrible = describle;
        this.mColor = color;
    }

    /**
     * 通过ResId获取美颜模式的文字，
     * mResId为0或context为空 时返回mDescrible
     */
    public String getMessage(Context context){
        if(context != null && mResId != 0){
            Resources res = context.getResources();
            if(res != null){
                return res.getString(mResId);
            }
        }
        return mDescrible;
    }

    public int getColor() {
        return mColor;
    }

    public int getFilterId(){
        return mFilterId;
    }

    public int getAlpha(){
        return mAlpha;
    }

    public int getId(){
        return mId;
    }
}
