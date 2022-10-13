package com.freewheelin.pulley.revision2021.channelio.channel;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.freewheelin.pulley.R;
import com.zoyi.channel.plugin.android.ChannelIO;
import com.zoyi.channel.plugin.android.model.rest.File;
import com.zoyi.channel.plugin.android.open.option.Language;
import com.zoyi.channel.plugin.android.store.SettingsStore;

import java.util.Locale;

public class PResUtils {
    public PResUtils() {
    }

    public static int getFileIconResourceId(File file) {
        if (file != null && file.getExtension() != null) {
            String var1 = file.getExtension();
            byte var2 = -1;
            switch(var1.hashCode()) {
                case -2000515510:
                    if (var1.equals("numbers")) {
                        var2 = 10;
                    }
                    break;
                case -907685685:
                    if (var1.equals("script")) {
                        var2 = 2;
                    }
                    break;
                case -900674644:
                    if (var1.equals("sketch")) {
                        var2 = 18;
                    }
                    break;
                case -887328209:
                    if (var1.equals("system")) {
                        var2 = 3;
                    }
                    break;
                case -820387517:
                    if (var1.equals("vector")) {
                        var2 = 20;
                    }
                    break;
                case -748101438:
                    if (var1.equals("archive")) {
                        var2 = 0;
                    }
                    break;
                case 3112:
                    if (var1.equals("ai")) {
                        var2 = 17;
                    }
                    break;
                case 99640:
                    if (var1.equals("doc")) {
                        var2 = 4;
                    }
                    break;
                case 103745:
                    if (var1.equals("hwp")) {
                        var2 = 7;
                    }
                    break;
                case 106079:
                    if (var1.equals("key")) {
                        var2 = 6;
                    }
                    break;
                case 110834:
                    if (var1.equals("pdf")) {
                        var2 = 12;
                    }
                    break;
                case 111220:
                    if (var1.equals("ppt")) {
                        var2 = 11;
                    }
                    break;
                case 111297:
                    if (var1.equals("psd")) {
                        var2 = 16;
                    }
                    break;
                case 118783:
                    if (var1.equals("xls")) {
                        var2 = 9;
                    }
                    break;
                case 120609:
                    if (var1.equals("zip")) {
                        var2 = 21;
                    }
                    break;
                case 3076010:
                    if (var1.equals("data")) {
                        var2 = 8;
                    }
                    break;
                case 3148879:
                    if (var1.equals("font")) {
                        var2 = 19;
                    }
                    break;
                case 3556653:
                    if (var1.equals("text")) {
                        var2 = 5;
                    }
                    break;
                case 93166550:
                    if (var1.equals("audio")) {
                        var2 = 14;
                    }
                    break;
                case 100313435:
                    if (var1.equals("image")) {
                        var2 = 1;
                    }
                    break;
                case 106426308:
                    if (var1.equals("pages")) {
                        var2 = 13;
                    }
                    break;
                case 112202875:
                    if (var1.equals("video")) {
                        var2 = 15;
                    }
            }

            switch(var2) {
                case 0:
                    return R.drawable.ch_plugin_file_zip;
                case 1:
                    return R.drawable.ch_plugin_file_image;
                case 2:
                    return R.drawable.ch_plugin_file_script;
                case 3:
                    return R.drawable.ch_plugin_file_system;
                case 4:
                    return R.drawable.ch_plugin_file_word;
                case 5:
                    return R.drawable.ch_plugin_file_document;
                case 6:
                    return R.drawable.ch_plugin_file_keynote;
                case 7:
                    return R.drawable.ch_plugin_file_hancom;
                case 8:
                    return R.drawable.ch_plugin_file_data;
                case 9:
                    return R.drawable.ch_plugin_file_excel;
                case 10:
                    return R.drawable.ch_plugin_file_numbers;
                case 11:
                    return R.drawable.ch_plugin_file_powerpoint;
                case 12:
                    return R.drawable.ch_plugin_file_pdf;
                case 13:
                    return R.drawable.ch_plugin_file_pages;
                case 14:
                    return R.drawable.ch_plugin_file_sound;
                case 15:
                    return R.drawable.ch_plugin_file_video;
                case 16:
                    return R.drawable.ch_plugin_file_photoshop;
                case 17:
                    return R.drawable.ch_plugin_file_ai;
                case 18:
                    return R.drawable.ch_plugin_file_sketchapp;
                case 19:
                    return R.drawable.ch_plugin_file_font;
                case 20:
                    return R.drawable.ch_plugin_file_vector;
                case 21:
                    return R.drawable.ch_plugin_file_zip;
                default:
                    return R.drawable.ch_plugin_file_else;
            }
        } else {
            return R.drawable.ch_plugin_file_else;
        }
    }

    public static String getString(String key) {
        return getString(PChannelIO.getAppContext(), key);
    }

    public static String getString(Context context, String key) {
        return getString(context, (Language) SettingsStore.get().language.get(), key);
    }

    public static String getString(Context context, @Nullable Language language, String key) {
        try {
            if (key != null) {
                int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
                if (resId != 0) {
                    if (language != null) {
                        return getLocaleStringResource(context, new Locale(language.toString()), resId);
                    }

                    return context.getResources().getString(resId);
                }
            }
        } catch (Exception var4) {
        }

        return key;
    }

    public static String getUnknown() {
        return getString(PChannelIO.getAppContext(), "ch.unknown");
    }

    public static String getLocaleStringResource(Context context, Locale requestedLocale, int resourceId) {
        String result;
        if (Build.VERSION.SDK_INT >= 17) {
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(requestedLocale);
            result = context.createConfigurationContext(config).getText(resourceId).toString();
        } else {
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, (DisplayMetrics)null);
            result = resources.getString(resourceId);
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, (DisplayMetrics)null);
        }

        return result;
    }

    public static int getColor(@ColorRes int colorId) {
        Context context = PChannelIO.getAppContext();
        return context != null ? ContextCompat.getColor(context, colorId) : -1;
    }

    public static int getColor(Context context, @ColorRes int colorId) {
        return context != null ? ContextCompat.getColor(context, colorId) : -1;
    }

    public static int getDimen(Context context, int resourceId) {
        return context.getResources().getDimensionPixelSize(resourceId);
    }
}
