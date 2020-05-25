/*
 * Copyright (c) 2017, 2018, 2019 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.debark.old;

import com.google.common.base.Joiner;
import net.minecraft.util.math.MathHelper;

public final class UCWColorspaceUtils {
    private static final float[] D65_WHITE = {0.9504f, 1.0000f, 1.0888f};
    private static final float E = 0.008856f;
    private static final float K = 903.3f;
    private static final float KE = K*E;
    private static final float E_CBRT = 0.2068930344f;


    private UCWColorspaceUtils() {

    }

    public static float sRGBtoLuma(float[] v) {
        float v1 = (float) (0.2126729*v[0] + 0.7151522*v[1] + 0.0721750*v[2]);
        float yr = v1 / D65_WHITE[1];
        float fy = (yr > E) ? (float) Math.cbrt(yr) : (K*yr + 16)/116.0f;
        return 116*fy - 16;
    }

    public static float[] XYZtoLAB(float[] v) {
        float xr = v[0] / D65_WHITE[0];
        float yr = v[1] / D65_WHITE[1];
        float zr = v[2] / D65_WHITE[2];

        float fx = (xr > E) ? (float) Math.cbrt(xr) : (K*xr + 16)/116.0f;
        float fy = (yr > E) ? (float) Math.cbrt(yr) : (K*yr + 16)/116.0f;
        float fz = (zr > E) ? (float) Math.cbrt(zr) : (K*zr + 16)/116.0f;

        return new float[] {
                116*fy - 16,
                500*(fx - fy),
                200*(fy - fz)
        };
    }

    public static float[] LABtoXYZ(float[] v) {
        float fy = (v[0] + 16)/116.0f;
        float fx = v[1]/500.0f + fy;
        float fz = fy - v[2]/200.0f;

        float yr;
        float xr = (fx > E_CBRT) ? (fx*fx*fx) : (116*fx - 16)/K;
        float zr = (fz > E_CBRT) ? (fz*fz*fz) : (116*fz - 16)/K;
        if (v[0] > KE) {
            yr = ((v[0]+16)/116.0f);
            yr *= yr * yr;
        } else {
            yr = v[0]/K;
        }

        return new float[] {
                xr * D65_WHITE[0],
                yr * D65_WHITE[1],
                zr * D65_WHITE[2]
        };
    }

    private static int asFF(float f) {
        if (f >= 1.0f) return 255;
        else if (f <= 0.0f) return 0;
        else return (Math.round(f * 255.0f) & 0xFF);
    }

    public static int asInt(float[] v) {
        return (asFF(v[0]) << 16) | (asFF(v[1]) << 8) | asFF(v[2]);
    }

    public static float[] fromInt(int v) {
        return new float[] {
                ((v >> 16) & 0xFF) / 255.0f,
                ((v >> 8) & 0xFF) / 255.0f,
                (v & 0xFF) / 255.0f
        };
    }

    public static float[] sRGBtoXYZ(float[] v) {
        return new float[] {
                (float) (0.4124564*v[0] + 0.3575761*v[1] + 0.1804375*v[2]),
                (float) (0.2126729*v[0] + 0.7151522*v[1] + 0.0721750*v[2]),
                (float) (0.0193339*v[0] + 0.1191920*v[1] + 0.9503041*v[2])
        };
    }

    public static float[] XYZtosRGB(float[] v) {
        return new float[] {
                (float) (3.2404542*v[0] + -1.5371385*v[1] + -0.4985314*v[2]),
                (float) (-0.9692660*v[0] + 1.8760108*v[1] + 0.0415560*v[2]),
                (float) (0.0556434*v[0] + -0.2040259*v[1] + 1.0572252*v[2])
        };
    }

    private static void printArray(float[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.printf(i > 0 ? ", " : "[");
            System.out.printf("%.3f", arr[i]);
        }
        System.out.println("]");
    }

    public static void main(String[] args) {
        int rgb = 0x800080;
        float[] tmp;
        printArray(tmp = fromInt(rgb));
        printArray(tmp = sRGBtoXYZ(tmp));
        printArray(tmp = XYZtoLAB(tmp));
        printArray(tmp = LABtoXYZ(tmp));
        printArray(tmp = XYZtosRGB(tmp));
    }
}
