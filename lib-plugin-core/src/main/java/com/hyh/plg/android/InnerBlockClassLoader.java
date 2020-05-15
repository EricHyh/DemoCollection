/*
 **        Droidblock Project
 **
 ** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
 **
 ** This file is part of Droidblock.
 **
 ** Droidblock is free software: you can redistribute it and/or
 ** modify it under the terms of the GNU Lesser General Public
 ** License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.
 **
 ** Droidblock is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 ** Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public
 ** License along with Droidblock.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
 **
 **/

package com.hyh.plg.android;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.yly.mob.utils.Logger;
import com.yly.mob.utils.dex.MultiDex;

import dalvik.system.DexClassLoader;

public class InnerBlockClassLoader extends DexClassLoader {


    public InnerBlockClassLoader(Context context, String apkfile, String optimizedDirectory, String libraryPath) {
        super(apkfile, optimizedDirectory, libraryPath, Context.class.getClassLoader());
        MultiDex.install(context, this, apkfile);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        if (Build.MANUFACTURER != null && TextUtils.equals("QIKU", Build.MANUFACTURER.toUpperCase())) {
            try {
                Class<?> clazz = findClass(className);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                Logger.d("in QIKU " + className + " not found");
            }
        }
        return super.loadClass(className, resolve);
    }
}
